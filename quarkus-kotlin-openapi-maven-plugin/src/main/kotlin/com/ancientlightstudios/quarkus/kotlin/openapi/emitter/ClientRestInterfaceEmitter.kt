package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.getDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.writeSerializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.TypeDefinitionUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import jakarta.ws.rs.core.Response

class ClientRestInterfaceEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        kotlinFile(clientPackage(), suite.name.extend(postfix = "Client")) {
            registerImport("jakarta.ws.rs.core", wildcardImport = true)
            registerImport("com.fasterxml.jackson.databind.ObjectMapper")
            registerImport(modelPackage(), wildcardImport = true)
            registerImport(apiPackage(), wildcardImport = true)
            registerImport("org.jboss.resteasy.reactive.RestResponse")
            registerImport("jakarta.enterprise.context.ApplicationScoped")
            registerImport("org.eclipse.microprofile.rest.client.inject.RestClient")
            registerImport("jakarta.ws.rs", wildcardImport = true)
            registerImport("java.util.concurrent.TimeoutException")
            registerImports(additionalImports)

            kotlinClass(fileName) {
                kotlinAnnotation("ApplicationScoped".rawClassName())

                kotlinMember("delegate".variableName(), suite.name.extend(postfix = "Delegate").typeName()) {
                    kotlinAnnotation("RestClient".rawClassName())
                }
                kotlinMember("objectMapper".variableName(), "ObjectMapper".rawTypeName())

                suite.requests.forEach {
                    generateRequest(it)
                }
            }
        }.also { generateFile(it) }
    }

    private fun KotlinClass.generateRequest(request: Request) {
        val returnType = request.name.className().extend(postfix = "Response")
        val httpResponseType = request.name.className().extend(postfix = "HttpResponse")
        val errorType = request.name.className().extend(postfix = "Error")

        kotlinMethod(request.name, true, returnType.typeName()) {


            request.parameters.forEach {
                kotlinParameter(it.name.variableName(), it.type.safeType) // TODO: default value for nullable properties
            }

            request.body?.let {
                kotlinParameter("body".variableName(), it.safeType) // TODO: default value for nullable properties
            }

            kotlinStatement {
                write("return try {")
                indent(newLineBefore = true, newLineAfter = true) {
                    // call delegate
                    writeln("val response = try {")
                    indent {
                        if (request.body != null) {
                            write("val bodyPayload:${"String".rawTypeName(request.body.nullable).render()} = objectMapper.writeValueAsString(")
                            writeSerializationStatement("body".variableName(), request.body)
                            writeln(")")
                        }
                        write("delegate.${request.name.render()}(")
                        val parameterNames = request.parameters.mapTo(mutableListOf()) { it.name.variableName().render() }
                        request.body?.let { parameterNames.add("bodyPayload") }
                        write(parameterNames.joinToString())
                        writeln(").toResponse()")
                    }
                    writeln("} catch (e: WebApplicationException) {")
                    indent {
                        writeln("e.response")
                    }
                    writeln("}")

                    writeln("val entity = response.readEntity(String::class.java)")
                    indent {
                        writeln(".parseAsJson(\"response.body\", objectMapper)")
                    }

                    writeln("val statusCode = RestResponse.Status.fromStatusCode(response.status)")
                    writeln()

                    write("val maybe: Maybe<${returnType.render()}> = when (statusCode) {")
                    indent(newLineBefore = true, newLineAfter = true) {
                        val defaultResponse = request.responses.firstOrNull { it.first == ResponseCode.Default }
                        request.responses.forEach { (responseCode, typeDefinitionUsage) ->
                            if (responseCode is ResponseCode.HttpStatusCode) {
                                emitResponseBranch(httpResponseType, responseCode, typeDefinitionUsage)
                            }
                        }

                        if (defaultResponse != null) {
                            emitDefaultResponseBranch(httpResponseType, defaultResponse.second)
                        } else {
                            writeln("else -> Maybe.Success(\"response.body\", ${errorType.render()}.ResponseError(\"unknown status code \${statusCode.name}\", response))")
                        }

                    }
                    writeln("}")
                    writeln()
                    write("when(maybe) {")
                    indent(newLineBefore = true, newLineAfter = true) {
                        writeln("is Maybe.Success -> maybe.value")
                        writeln("is Maybe.Failure -> ${errorType.render()}.ResponseError(maybe.errors.joinToString { it.message }, response)")
                    }
                    write("}")
                }


                writeln("} catch (_: TimeoutException) {")
                indent {
                    writeln("${errorType.render()}.RequestError(RequestErrorReason.Timeout)")
                }
                write("} catch (e: Exception) {")
                indent(newLineBefore = true, newLineAfter = true) {
                    writeln("// TODO: check exception type")
                    write("${errorType.render()}.RequestError(RequestErrorReason.Unknown)")
                }
                write("}")
            }
        }
    }

    private fun CodeWriter.emitResponseBranch(
        returnType: ClassName,
        responseCode: ResponseCode.HttpStatusCode,
        typeDefinitionUsage: TypeDefinitionUsage?
    ) {
        write("RestResponse.Status.${Response.Status.fromStatusCode(responseCode.value).name} -> {")
        indent(newLineBefore = true, newLineAfter = true) {
            if (typeDefinitionUsage != null) {
                emitTypeConversion(typeDefinitionUsage)
                write("maybe.onSuccess { success(${returnType.render()}.${responseCode.statusCodeReason()}(value)) }")
            } else {
                write("Maybe.Success(\"response.body\", ${returnType.render()}.${responseCode.statusCodeReason()}())")
            }
        }
        writeln("}")
    }

    private fun CodeWriter.emitDefaultResponseBranch(
        returnType: ClassName,
        typeDefinitionUsage: TypeDefinitionUsage?
    ) {
        write("else -> {")
        indent(newLineBefore = true, newLineAfter = true) {
            if (typeDefinitionUsage != null) {
                emitTypeConversion(typeDefinitionUsage)
                write("maybe.onSuccess { success(${returnType.render()}.Default(statusCode, value)) }")
            } else {
                write("Maybe.Success(\"response.body\", ${returnType.render()}.Default(statusCode))")
            }
        }
        writeln("}")
    }

    private fun CodeWriter.emitTypeConversion(typeDefinitionUsage: TypeDefinitionUsage) {
        val parameter = "maybe".variableName()
        val source = "entity".variableName().pathExpression()
        getDeserializationStatement(source, parameter, typeDefinitionUsage, true).render(this)
        writeln(forceNewLine = false)
    }

    private fun ResponseCode.HttpStatusCode.statusCodeReason() =
        Response.Status.fromStatusCode(value)?.reasonPhrase?.className()?.render() ?: "status${value}"

}
