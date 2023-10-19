package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.TypeDefinitionUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import jakarta.ws.rs.core.Response

class ServerResponseContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        suite.requests.forEach {
            emitResponseContainer(it)
        }
    }

    private fun EmitterContext.emitResponseContainer(request: Request) {
        kotlinFile(serverPackage(), request.name.extend(postfix = "Response").className()) {
            registerImport("org.jboss.resteasy.reactive.RestResponse.ResponseBuilder")
            registerImport("org.jboss.resteasy.reactive.RestResponse")
            registerImport(modelPackage(), wildcardImport = true)

            val defaultResponseExists = request.responses.any { it.first == ResponseCode.Default }

            kotlinClass(fileName, true) {
                kotlinMember("response".variableName(), "RestResponse".rawTypeName().of("*".rawClassName()), private = false) {}
                kotlinCompanion {
                    emitGenericStatusMethod(fileName, defaultResponseExists)

                    request.responses.forEach {
                        when (val code = it.first) {
                            is ResponseCode.HttpStatusCode -> emitStatusMethod(code, it.second)
                            is ResponseCode.Default -> emitDefaultStatusMethod(it.second)
                        }
                    }
                }
            }
        }.also { generateFile(it) }
    }

    private fun KotlinCompanion.emitGenericStatusMethod(className: ClassName, defaultResponseExists: Boolean) {
        kotlinMethod("status".methodName(), bodyAsAssignment = true, private = defaultResponseExists) {
            val statusVariable = "status".variableName()
            val bodyVariable = "body".variableName()
            kotlinParameter(statusVariable, "Int".rawTypeName())
            kotlinParameter(bodyVariable, "Any".rawTypeName(true))

            kotlinStatement {
                write("${className.render()}(ResponseBuilder.create<Any?>")
                write("(RestResponse.Status.fromStatusCode(${statusVariable.render()}), ${bodyVariable.render()})")
                write(".build())")
            }
        }
    }

    private fun KotlinCompanion.emitStatusMethod(statusCode: ResponseCode.HttpStatusCode, type: TypeDefinitionUsage?) {
        kotlinMethod(statusCode.value.statusCodeReason().methodName(), bodyAsAssignment = true) {
            if (type != null) {
                val bodyVariable = "body".variableName()
                kotlinParameter(bodyVariable, type.safeType)
                kotlinStatement {
                    write("status(${statusCode.value}, ${bodyVariable.render()})")
                }
            } else {
                kotlinStatement {
                    write("status(${statusCode.value}, null)")
                }
            }
        }
    }

    private fun KotlinCompanion.emitDefaultStatusMethod(type: TypeDefinitionUsage?) {
        kotlinMethod("defaultStatus".methodName(), bodyAsAssignment = true) {
            val statusVariable = "status".variableName()
            kotlinParameter(statusVariable, "Int".rawTypeName())
            if (type != null) {
                val bodyVariable = "body".variableName()
                kotlinParameter(bodyVariable, type.safeType)
                kotlinStatement {
                    write("status(${statusVariable.render()}, ${bodyVariable.render()})")
                }
            } else {
                kotlinStatement {
                    write("status(${statusVariable.render()}, null)")
                }
            }
        }
    }

    private fun Int.statusCodeReason() = Response.Status.fromStatusCode(this)?.reasonPhrase ?: "status${this}"
}
