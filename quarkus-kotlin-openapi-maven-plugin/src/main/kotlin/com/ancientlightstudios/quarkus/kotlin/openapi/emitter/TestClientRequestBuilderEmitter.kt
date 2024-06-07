package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBuilderClassNameHint.requestBuilderClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.refactoring.AssignContentTypesRefactoring.Companion.getContentTypeForFormPart
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class TestClientRequestBuilderEmitter : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this
        spec.inspect {
            bundles {
                requests {
                    emitRequestBuilderFile()
                        .writeFile()
                }
            }
        }
    }

    private fun RequestInspection.emitRequestBuilderFile() = kotlinFile(request.requestBuilderClassName) {
        val requestSpecificationVariable = "requestSpecification".variableName()
        kotlinClass(fileName) {
            registerImports(Library.AllClasses)

            kotlinMember(
                requestSpecificationVariable,
                type = RestAssured.RequestSpecificationClass.typeName(),
                mutable = true,
                accessModifier = null
            )
            kotlinMember(
                "objectMapper".variableName(),
                type = Misc.ObjectMapperClass.typeName()
            )

            parameters {
                if (parameter.kind != ParameterKind.Path) {
                    kotlinMethod(parameter.name.methodName()) {
                        kotlinParameter("value".variableName(), parameter.content.typeUsage.buildValidType())

                        val methodName = when (parameter.kind) {
                            ParameterKind.Query -> "queryParams"
                            ParameterKind.Header -> "headers"
                            ParameterKind.Cookie -> "cookies"
                            ParameterKind.Path -> "lazyVogel" // filtered out above
                        }.methodName()

                        val builder: (StatementAware.() -> Unit) = {
                            val argument = invoke(
                                "mapOf".methodName(),
                                invoke(
                                    Kotlin.PairClass.constructorName,
                                    parameter.name.literal(),
                                    "value".variableName()
                                )
                            )

                            requestSpecificationVariable.invoke(methodName, argument)
                                .assignment(requestSpecificationVariable)
                        }

                        if (parameter.content.typeUsage.nullable && !parameter.content.typeUsage.required) {
                            "value".variableName().nullCheck().invoke("let".methodName()) {
                                builder()
                            }.statement()
                        } else {
                            builder()
                        }
                    }
                }
            }

            body {
                body.emitBodyMethods(this@kotlinClass, requestSpecificationVariable)
            }
        }
    }

    private fun TransformableBody.emitBodyMethods(clazz: KotlinClass, requestSpecificationVariable: VariableName) {
        // TODO: support more than just json
        when (content.mappedContentType) {
            ContentType.ApplicationJson -> emitJsonBodyMethod(clazz, requestSpecificationVariable)
            ContentType.TextPlain -> emitPlainBodyMethod(clazz, requestSpecificationVariable)
            ContentType.ApplicationFormUrlencoded -> emitFormBodyMethod(clazz, requestSpecificationVariable)
            ContentType.MultipartFormData -> ProbableBug("Multipart-Form not yet supported for test client")
            ContentType.ApplicationOctetStream -> ProbableBug("Octet-Stream not yet supported for test client")
        }
    }

    private fun TransformableBody.emitJsonBodyMethod(clazz: KotlinClass, requestSpecificationVariable: VariableName) {
        clazz.kotlinMethod("body".methodName()) {
            kotlinParameter("value".variableName(), content.typeUsage.buildValidType())

            val bodyStatement = emitterContext.runEmitter(
                SerializationStatementEmitter(content.typeUsage, "value".variableName(), content.mappedContentType)
            ).resultStatement

            requestSpecificationVariable
                .invoke("contentType".methodName(), content.rawContentType.literal())
                .invoke(
                    "body".methodName(),
                    "objectMapper".variableName().invoke("writeValueAsString".rawMethodName(), bodyStatement)
                )
                .assignment(requestSpecificationVariable)
        }
    }

    private fun TransformableBody.emitPlainBodyMethod(clazz: KotlinClass, requestSpecificationVariable: VariableName) {
        clazz.kotlinMethod("body".methodName()) {
            kotlinParameter("value".variableName(), content.typeUsage.buildValidType())

            val bodyStatement = emitterContext.runEmitter(
                SerializationStatementEmitter(content.typeUsage, "value".variableName(), content.mappedContentType)
            ).resultStatement

            requestSpecificationVariable
                .invoke("contentType".methodName(), content.rawContentType.literal())
                .invoke("body".methodName(), bodyStatement)
                .assignment(requestSpecificationVariable)
        }
    }

    private fun TransformableBody.emitFormBodyMethod(clazz: KotlinClass, requestSpecificationVariable: VariableName) {
        clazz.kotlinMethod("body".methodName()) {
            val typeUsage = content.typeUsage
            kotlinParameter("value".variableName(), typeUsage.buildValidType())

            var statement = requestSpecificationVariable
                .invoke("contentType".methodName(), content.rawContentType.literal())


            val safeType = typeUsage.type
            if (safeType is ObjectTypeDefinition) {
                val baseStatement = if (typeUsage.nullable) {
                    "value".variableName().nullCheck()
                } else {
                    "value".variableName()
                }

                // TODO: in case of json we probably want the writeValueAsString method to convert the payload
                //   see jsonBody. same for restClient
                safeType.properties.forEach {
                    val propertyType = it.typeUsage
                    val contentType = getContentTypeForFormPart(propertyType.type)
                    val propertyStatement =
                        emitterContext.runEmitter(
                            SerializationStatementEmitter(propertyType, baseStatement.property(it.name), contentType)
                        ).resultStatement

                    statement =
                        statement.wrap().invoke("formParam".methodName(), it.sourceName.literal(), propertyStatement)
                }
            } else {
                val serializeStatement = emitterContext.runEmitter(
                    SerializationStatementEmitter(typeUsage, "value".variableName(), content.mappedContentType)
                ).resultStatement

                statement = statement.invoke("formParam".methodName(), parameterVariableName, serializeStatement)
            }

            statement.assignment(requestSpecificationVariable)
        }
    }

    /*
fun body(value: UnsafeJson<SimpleForm>) {
}
*/

}