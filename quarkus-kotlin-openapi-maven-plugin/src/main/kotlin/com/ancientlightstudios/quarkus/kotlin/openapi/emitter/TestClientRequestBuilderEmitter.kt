package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBuilderClassNameHint.requestBuilderClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IfElseExpression.Companion.ifElseExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.refactoring.AssignContentTypesRefactoring.Companion.getContentTypeForFormPart

//class TestClientRequestBuilderEmitter : CodeEmitter {
//
//    private lateinit var emitterContext: EmitterContext
//
//    override fun EmitterContext.emit() {
//        emitterContext = this
//        spec.inspect {
//            bundles {
//                requests {
//                    emitRequestBuilderFile()
//                        .writeFile()
//                }
//            }
//        }
//    }
//
//    private fun RequestInspection.emitRequestBuilderFile() = kotlinFile(request.requestBuilderClassName) {
//        val requestSpecificationVariable = "requestSpecification".variableName()
//        kotlinClass(fileName) {
//            registerImports(Library.AllClasses)
//            registerImports(emitterContext.getAdditionalImports())
//
//            kotlinMember(
//                requestSpecificationVariable,
//                type = RestAssured.RequestSpecificationClass.typeName(),
//                mutable = true,
//                accessModifier = null
//            )
//            kotlinMember(
//                "objectMapper".variableName(),
//                type = Misc.ObjectMapperClass.typeName()
//            )
//
//            parameters {
//                if (parameter.kind != ParameterKind.Path) {
//                    kotlinMethod(parameter.name.methodName()) {
//                        val nullableType = parameter.content.typeUsage.forceNullable()
//
//                        // TODO: the buildValidType might revert the nullable again if this type has a default value
//                        kotlinParameter("value".variableName(), nullableType.buildValidType())
//
//                        val methodName = when (parameter.kind) {
//                            ParameterKind.Query -> "queryParams"
//                            ParameterKind.Header -> "headers"
//                            ParameterKind.Cookie -> "cookies"
//                            ParameterKind.Path -> "lazyVogel" // filtered out above
//                        }.methodName()
//
//                        val parameterStatement = emitterContext.runEmitter(
//                            SerializationStatementEmitter(
//                                nullableType,
//                                "value".variableName(),
//                                parameter.content.mappedContentType,
//                                true // we only use the statement inside a null check
//                            )
//                        ).resultStatement
//
//                        // produces
//                        //
//                        // if (value != null) {
//                        //     requestSpecification = requestSpecification.<methodName>(mapOf(Pair(<parameter.name>, <parameterStatement>)))
//                        // }
//                        ifElseExpression("value".variableName().compareWith(nullLiteral(), "!=")) {
//                            val argument = InvocationExpression.invoke(
//                                "mapOf".methodName(),
//                                InvocationExpression.invoke(
//                                    Kotlin.PairClass.constructorName,
//                                    parameter.name.literal(),
//                                    parameterStatement
//                                )
//                            )
//
//                            requestSpecificationVariable.invoke(methodName, argument)
//                                .assignment(requestSpecificationVariable)
//                        }.statement()
//                    }
//                }
//            }
//
//            body {
//                body.emitBodyMethods(this@kotlinClass, requestSpecificationVariable)
//            }
//        }
//    }
//
//    private fun OpenApiBody.emitBodyMethods(clazz: KotlinClass, requestSpecificationVariable: VariableName) {
//        when (content.mappedContentType) {
//            ContentType.ApplicationJson -> emitJsonBodyMethod(clazz, requestSpecificationVariable)
//            ContentType.TextPlain -> emitPlainBodyMethod(clazz, requestSpecificationVariable)
//            ContentType.ApplicationFormUrlencoded -> emitFormBodyMethod(clazz, requestSpecificationVariable)
//            ContentType.ApplicationOctetStream -> emitOctetStreamBodyMethod(clazz, requestSpecificationVariable)
////            ContentType.MultipartFormData -> ProbableBug("Multipart-Form not yet supported for test client")
//        }
//    }
//
//    private fun specialMapSupport(type: TypeDefinition): Boolean {
//        if (type !is ObjectTypeDefinition) {
//            return false
//        }
//
//        if (!type.isPureMap) {
//            return false
//        }
//
//        val valueType = type.additionalProperties!!
//        return valueType.type is PrimitiveTypeDefinition || valueType.type is EnumTypeDefinition
//    }
//
//    private fun OpenApiBody.emitJsonBodyMethod(clazz: KotlinClass, requestSpecificationVariable: VariableName) {
//        val specialMapSupport = specialMapSupport(content.typeUsage.type)
//
//        if (!specialMapSupport) {
//            // generate the default body method
//            clazz.kotlinMethod("body".methodName()) {
//                val nullableType = content.typeUsage.forceNullable()
//                kotlinParameter("value".variableName(), nullableType.buildValidType())
//
//                val bodyStatement = emitterContext.runEmitter(
//                    SerializationStatementEmitter(
//                        nullableType,
//                        "value".variableName(),
//                        content.mappedContentType
//                    )
//                ).resultStatement
//
//                // produces
//                //
//                // requestSpecification = requestSpecification.body(<bodyStatement>)
//                requestSpecificationVariable
//                    .invoke(
//                        "body".methodName(),
//                        bodyStatement.invoke("asString".methodName(), "objectMapper".variableName())
//                    )
//                    .assignment(requestSpecificationVariable)
//            }
//        }
//
//        // generate a body method with a UnsafeJson parameter for object and oneOf types
//        if (content.typeUsage.type is ObjectTypeDefinition || content.typeUsage.type is OneOfTypeDefinition || content.typeUsage.type is CollectionTypeDefinition) {
//            clazz.kotlinMethod("body".methodName()) {
//                kotlinAnnotation(Kotlin.JvmNameClass, "name".variableName() to "bodyWithUnsafe".literal())
//                kotlinParameter("value".variableName(), content.typeUsage.buildUnsafeJsonType(specialMapSupport))
//
//                val bodyStatement = emitterContext.runEmitter(
//                    UnsafeSerializationStatementEmitter(
//                        content.typeUsage,
//                        "value".variableName(),
//                        content.mappedContentType,
//                        !specialMapSupport // the normal type is not null here, only for special maps
//                    )
//                ).resultStatement
//
//                // produces
//                //
//                // requestSpecification = requestSpecification.body(<bodyStatement>)
//                requestSpecificationVariable
//                    .invoke(
//                        "body".methodName(),
//                        bodyStatement.invoke("asString".methodName(), "objectMapper".variableName())
//                    )
//                    .assignment(requestSpecificationVariable)
//            }
//        }
//    }
//
//    private fun OpenApiBody.emitPlainBodyMethod(clazz: KotlinClass, requestSpecificationVariable: VariableName) {
//        clazz.kotlinMethod("body".methodName()) {
//            val nullableType = content.typeUsage.forceNullable()
//            kotlinParameter("value".variableName(), nullableType.buildValidType())
//
//            val bodyStatement = emitterContext.runEmitter(
//                SerializationStatementEmitter(
//                    nullableType,
//                    "value".variableName(),
//                    content.mappedContentType,
//                    true // we only use the statement inside a null check
//                )
//            ).resultStatement
//
//            // produces
//            //
//            // if (value != null) {
//            //     requestSpecification = requestSpecification.body(<bodyStatement>)
//            // }
//            ifElseExpression("value".variableName().compareWith(nullLiteral(), "!=")) {
//                requestSpecificationVariable
//                    .invoke("body".methodName(), bodyStatement)
//                    .assignment(requestSpecificationVariable)
//            }.statement()
//        }
//    }
//
//    private fun OpenApiBody.emitFormBodyMethod(clazz: KotlinClass, requestSpecificationVariable: VariableName) {
//        clazz.kotlinMethod("body".methodName()) {
//            val nullableType = content.typeUsage.forceNullable()
//            kotlinParameter("value".variableName(), nullableType.buildValidType())
//
//            // produces
//            //
//            // if (value != null) {
//            //     ...
//            // }
//            ifElseExpression("value".variableName().compareWith(nullLiteral(), "!=")) {
//                val safeType = nullableType.type
//                if (safeType is ObjectTypeDefinition) {
//                    // TODO: in case of json we probably want the writeValueAsString method to convert the payload
//                    //   see jsonBody. same for restClient
//                    safeType.properties.forEach {
//                        val propertyType = it.typeUsage
//                        val contentType = getContentTypeForFormPart(propertyType.type)
//                        if (propertyType.nullable) {
//                            ifElseExpression(
//                                "value".variableName().property(it.name).compareWith(nullLiteral(), "!=")
//                            ) {
//                                renderFormParamStatement(
//                                    requestSpecificationVariable,
//                                    propertyType,
//                                    "value".variableName().property(it.name),
//                                    contentType,
//                                    it.sourceName
//                                )
//                            }
//                        } else {
//                            renderFormParamStatement(
//                                requestSpecificationVariable,
//                                propertyType,
//                                "value".variableName().property(it.name),
//                                contentType,
//                                it.sourceName
//                            )
//                        }
//                    }
//                } else {
//                    renderFormParamStatement(
//                        requestSpecificationVariable,
//                        nullableType,
//                        "value".variableName(),
//                        content.mappedContentType,
//                        parameterVariableName.value
//                    )
//                }
//            }.statement()
//        }
//    }
//
//    private fun StatementAware.renderFormParamStatement(
//        requestSpecificationVariable: VariableName,
//        type: TypeUsage,
//        parameter: KotlinExpression,
//        contentType: ContentType,
//        parameterName: String
//    ) {
//        val serializeStatement = emitterContext.runEmitter(
//            SerializationStatementEmitter(
//                type,
//                parameter,
//                contentType,
//                true // we only use the statement inside a null check
//            )
//        ).resultStatement
//
//        requestSpecificationVariable
//            .invoke("formParam".methodName(), parameterName.literal(), serializeStatement)
//            .assignment(requestSpecificationVariable)
//
//    }
//
//
//    private fun emitOctetStreamBodyMethod(
//        clazz: KotlinClass,
//        requestSpecificationVariable: VariableName
//    ) {
//        clazz.kotlinMethod("body".methodName()) {
//            kotlinParameter("value".variableName(), Kotlin.ByteArrayClass.typeName(true))
//
//            // produces
//            //
//            // if (value != null) {
//            //     requestSpecification = requestSpecification.body(value)
//            // }
//            ifElseExpression("value".variableName().compareWith(nullLiteral(), "!=")) {
//                requestSpecificationVariable
//                    .invoke("body".methodName(), "value".variableName())
//                    .assignment(requestSpecificationVariable)
//            }.statement()
//        }
//    }
//
//    private fun TypeUsage.forceNullable() = TypeUsage(false, this.type)
//}
