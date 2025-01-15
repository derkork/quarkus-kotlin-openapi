package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IfElseExpression.Companion.ifElseExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.RequestParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.TestClientRequestBuilder
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class TestClientRequestBuilderEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<TestClientRequestBuilder>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(builder: TestClientRequestBuilder) {
        kotlinFile(builder.name.asTypeName()) {

            kotlinClass(name) {
                registerImports(Library.All)
                registerImports(config.additionalImports())

                kotlinMember(
                    "requestSpecification",
                    type = RestAssured.RequestSpecification.asTypeReference(),
                    mutable = true,
                    accessModifier = null
                )
                kotlinMember("dependencyVogel", builder.dependencyVogel.name.asTypeReference())

                val context = object : TestClientRequestBuilderHandlerContext {
                    override fun addMethod(method: KotlinMethod) = this@kotlinClass.addMethod(method)
                }

                builder.parameters.filterNot { it.kind == ParameterKind.Path }.forEach { parameter ->
                    getHandler<TestClientRequestBuilderHandler, Unit> {
                        context.emitParameter(parameter)
                    }
                }

                builder.body?.let { body ->
                    getHandler<TestClientRequestBuilderHandler, Unit> {
                        context.emitBody(body)
                    }
                }
            }
        }
    }

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

}

interface TestClientRequestBuilderHandlerContext : MethodAware {

    /**
     * generates a default method for the parameter. if the type is nullable, a null check will be added, so the given
     * serialization should ignore null values
     */
    fun emitDefaultParameter(parameter: RequestParameter, type: KotlinTypeReference, serialization: KotlinExpression) {
        kotlinMethod(parameter.name) {
            kotlinParameter("value", type)

            val methodName = when (parameter.kind) {
                ParameterKind.Query -> "queryParams"
                ParameterKind.Header -> "headers"
                ParameterKind.Cookie -> "cookies"
                ParameterKind.Path -> ProbableBug("path params are not supported by the test client builder")
            }

            // produces
            // requestSpecification.<methodName>(mapOf(Pair(<parameter.sourceName>, <serialization>)))
            val pair = invoke(Kotlin.Pair.identifier(), parameter.sourceName.literal(), serialization)
            val statement = "requestSpecification".identifier().invoke(methodName, invoke("mapOf", pair))

            // produces
            // if (value != null) {
            //     requestSpecification = <statement>
            // }
            // or just
            // requestSpecification = <statement>
            // if the type is not nullable
            if (type.nullable) {
                ifElseExpression("value".identifier().compareWith(nullLiteral(), "!=")) {
                    statement.assignment("requestSpecification")
                }.statement()
            } else {
                statement.assignment("requestSpecification")
            }
        }
    }

    /**
     * generates a default method for the body. if the type is nullable, a null check will be added, so the given
     * serialization should ignore null values
     */
    fun emitDefaultBody(body: RequestBody, type: KotlinTypeReference, serialization: KotlinExpression) {
        kotlinMethod(body.name) {
            kotlinParameter("value", type)

            // produces
            // requestSpecification.body(<serialization>)
            val statement = "requestSpecification".identifier().invoke("body", serialization)

            // produces
            // if (value != null) {
            //     requestSpecification = <statement>
            // }
            // or just
            // requestSpecification = <statement>
            // if the type is not nullable
            if (type.nullable) {
                ifElseExpression("value".identifier().compareWith(nullLiteral(), "!=")) {
                    statement.assignment("requestSpecification")
                }.statement()
            } else {
                statement.assignment("requestSpecification")
            }
        }
    }

}

interface TestClientRequestBuilderHandler : Handler {

    fun TestClientRequestBuilderHandlerContext.emitParameter(parameter: RequestParameter): HandlerResult<Unit>

    fun TestClientRequestBuilderHandlerContext.emitBody(body: RequestBody): HandlerResult<Unit>

}