package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.getClientTransformStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
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

            kotlinClass(fileName, false) {
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
        kotlinMethod(request.name, true, "RequestResult".rawTypeName().of(returnType)) {
            request.parameters.forEach {
                kotlinParameter(it.name, it.type.safeType) // TODO: default value for nullable properties
            }

            request.body?.let {
                kotlinParameter("body".variableName(), it.safeType) // TODO: default value for nullable properties
            }

            kotlinStatement {
                write("return try {")
                indent(newLineBefore = true, newLineAfter = true) {
                    // call delegate
                    write("val response = delegate.${request.name.render()}(")
                    val parameterNames = request.parameters.mapTo(mutableListOf()) { it.name.render() }
                    request.body?.let { parameterNames.add("body") }
                    write(parameterNames.joinToString())
                    writeln(")")

                    writeln("val statusCode = RestResponse.Status.fromStatusCode(response.status)")
                    writeln()

                    write("val maybe: Maybe<RequestResult<${returnType.render()}>> = when (statusCode) {")
                    indent(newLineBefore = true, newLineAfter = true) {
                        val defaultResponse = request.responses.firstOrNull { it.first == ResponseCode.Default }
                        request.responses.forEach { (responseCode, typeDefinitionUsage) ->
                            if (responseCode is ResponseCode.HttpStatusCode) {
                                emitResponseBranch(returnType, responseCode, typeDefinitionUsage)
                            }
                        }

                        if (defaultResponse != null) {
                            emitDefaultResponseBranch(returnType, defaultResponse.second)
                        } else {
                            writeln("else -> Maybe.Success(\"response.body\", RequestResult.ResponseError(\"unknown status code \${statusCode.name}\", response))")
                        }

                    }
                    writeln("}")
                    writeln()
                    write("when(maybe) {")
                    indent(newLineBefore = true, newLineAfter = true) {
                        writeln("is Maybe.Success -> maybe.value")
                        writeln("is Maybe.Failure -> RequestResult.ResponseError(maybe.errors.joinToString { it.message }, response)")
                    }
                    write("}")
                }
                write("} catch (e: Exception) {")
                indent(newLineBefore = true, newLineAfter = true) {
                    writeln("// TODO: check exception type")
                    write("RequestResult.RequestError(RequestErrorReason.Unknown)")
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
                write(".onSuccess { success(RequestResult.Response(${returnType.render()}.${responseCode.statusCodeReason()}(value))) }")
            } else {
                write("Maybe.Success(\"response.body\", RequestResult.Response(${returnType.render()}.${responseCode.statusCodeReason()}()))")
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
                write(".onSuccess { success(RequestResult.Response(${returnType.render()}.Default(statusCode, value))) }")
            } else {
                write("Maybe.Success(\"response.body\", RequestResult.Response(${returnType.render()}.Default(statusCode)))")
            }
        }
        writeln("}")
    }

    private fun CodeWriter.emitTypeConversion(typeDefinitionUsage: TypeDefinitionUsage) {
        getClientTransformStatement(
            "response".variableName().pathExpression().then("entity".variableName()),
            typeDefinitionUsage, "response.body".stringExpression()
        ).render(this)
        writeln(forceNewLine = false)
    }

    private fun ResponseCode.HttpStatusCode.statusCodeReason() =
        Response.Status.fromStatusCode(value)?.reasonPhrase?.className()?.render() ?: "status${value}"

}