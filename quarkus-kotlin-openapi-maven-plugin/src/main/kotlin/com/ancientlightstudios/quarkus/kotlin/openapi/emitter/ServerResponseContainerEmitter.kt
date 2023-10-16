package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.VariableName.Companion.variableName
import jakarta.ws.rs.core.Response

class ServerResponseContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite) {
        suite.requests.forEach {
            emitResponseContainer(it)
        }
    }

    private fun EmitterContext.emitResponseContainer(request: Request) {
        val fileName = request.name.extend(postfix = "Response").className()
        kotlinFile(serverPackage(), fileName) {
            registerImport("org.jboss.resteasy.reactive.RestResponse.ResponseBuilder")
            registerImport("org.jboss.resteasy.reactive.RestResponse")

            val defaultResponseExists = request.responses.any { it.first == ResponseCode.Default }

            kotlinClass(fileName, true) {
                kotlinMember("response".variableName(), "RestResponse".rawTypeName().of("*".rawClassName())) {}
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

    private fun KotlinCompanion.emitStatusMethod(statusCode: ResponseCode.HttpStatusCode, type: TypeName?) {
        kotlinMethod(statusCode.value.statusCodeReason().methodName(), bodyAsAssignment = true) {
            if (type != null) {
                val bodyVariable = "body".variableName()
                kotlinParameter(bodyVariable, type)
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

    private fun KotlinCompanion.emitDefaultStatusMethod(type: TypeName?) {
        kotlinMethod("defaultStatus".methodName(), bodyAsAssignment = true) {
            val statusVariable = "status".variableName()
            kotlinParameter(statusVariable, "Int".rawTypeName())
            if (type != null) {
                val bodyVariable = "body".variableName()
                kotlinParameter(bodyVariable, type)
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
}

fun Int.statusCodeReason() = Response.Status.fromStatusCode(this)?.reasonPhrase ?: "status${this}"
