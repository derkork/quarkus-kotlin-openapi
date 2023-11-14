package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.RequestBuilderDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.getDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ArrayExpression.Companion.arrayExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

class ServerRestInterfaceEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        kotlinFile(serverPackage(), suite.name.extend(postfix = "Server")) {
            registerImport("jakarta.ws.rs", wildcardImport = true)
            registerImport("com.fasterxml.jackson.databind.ObjectMapper")
            registerImport(modelPackage(), wildcardImport = true)
            registerImport(apiPackage(), wildcardImport = true)
            registerImport("org.jboss.resteasy.reactive.RestResponse")
            validatorPackage?.let {
                registerImport(it, wildcardImport = true)
            }

            kotlinClass(fileName) {
                addPathAnnotation(pathPrefix)

                kotlinMember("delegate".variableName(), suite.name.extend(postfix = "Delegate").typeName())
                kotlinMember("objectMapper".variableName(), "ObjectMapper".rawTypeName())

                suite.requests.forEach {
                    generateRequest(it)
                }
            }
        }.also { generateFile(it) }
    }

    private fun KotlinClass.generateRequest(request: Request) {
        kotlinMethod(request.name, true, "RestResponse<*>".rawTypeName()) {
            kotlinAnnotation(request.method.name.uppercase().rawClassName())
            addPathAnnotation(request.path)
            if (request.responses.any { it.second != null }) {
                kotlinAnnotation(
                    "Produces".rawClassName(),
                    "value".variableName() to "application/json".stringExpression().arrayExpression()
                )
            }

            val statement = RequestBuilderDeserializationStatement(
                request.name, request.name.extend(postfix = "Request").className()
            )

            request.parameters.forEach {
                kotlinParameter(it.name.variableName(), it.type.unsafeType) {
                    kotlinAnnotation(
                        it.source.value.rawClassName(),
                        "value".variableName() to it.name.stringExpression()
                    )
                }
                val parameter = it.name.variableName().extend(postfix = "maybe")
                val source = it.name.variableName().parameterToMaybeExpression(
                    "request.${it.source.name.lowercase()}.${it.name}".stringExpression()
                )
                addStatement(getDeserializationStatement(source, parameter, it.type, false))
                statement.addParameter(parameter)
            }

            request.body?.let {
                kotlinParameter("body".variableName(), "String".rawTypeName(true))
                kotlinAnnotation(
                    "Consumes".rawClassName(),
                    "value".variableName() to "application/json".stringExpression().arrayExpression()
                )

                kotlinStatement {
                    write("val node = body.parseAsJson(\"request.body\", objectMapper)")
                }
                val parameter = "body".variableName().extend(postfix = "maybe")
                val source = "node".variableName().pathExpression()
                addStatement(getDeserializationStatement(source, parameter, it, true))
                statement.addParameter(parameter)
            }

            addStatement(statement)
        }
    }
}
