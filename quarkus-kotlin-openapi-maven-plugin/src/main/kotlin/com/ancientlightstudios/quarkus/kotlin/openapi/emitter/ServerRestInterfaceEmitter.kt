package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestMethodNameHint.requestMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceClassNameHint.serverRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName

class ServerRestInterfaceEmitter(private val pathPrefix: String) : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            bundles {
                emitRestInterfaceFile().writeFile()
            }
        }
    }

    private fun RequestBundleInspection.emitRestInterfaceFile() = kotlinFile(bundle.serverRestInterfaceClassName) {
        kotlinClass(fileName) {
            addPathAnnotation(pathPrefix)

            kotlinMember("delegate".variableName(), bundle.serverDelegateClassName.typeName())
            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())

            requests {
                emitRequest(this@kotlinClass)
            }
        }
    }

    private fun RequestInspection.emitRequest(containerClass: KotlinClass) = with(containerClass) {
        kotlinMethod(request.requestMethodName, true, Misc.RestResponseClass.typeName().of(Kotlin.Star)) {
            addRequestMethodAnnotation(request.method)
            addPathAnnotation(request.path)

            // TODO: is this still correct, if we support different content-types for a request (e.g. 200 as text/plain, but 404 as application/json or even multiple for the same code)
            val contentTypes = request.responses.flatMap { it.body?.contentTypes ?: emptyList() }.toSet()
            if (contentTypes.isNotEmpty()) {
                addProducesAnnotation(contentTypes)
            }

            parameters {
                // TODO: replace with the correct type, e.g. List<...>
                kotlinParameter(parameter.parameterVariableName, Kotlin.StringClass.typeName(true)) {
                    addSourceAnnotation(parameter)
                }

//                val parameter = it.name.variableName().extend(postfix = "maybe")
//                val source = it.name.variableName().parameterToMaybeExpression(
//                    "request.${it.source.name.lowercase()}.${it.name}".stringExpression()
//                )
//                addStatement(getDeserializationStatement(source, parameter, it.type, false))
//                statement.addParameter(parameter)
            }

            body {
                val rawVariable = "body".variableName()
                kotlinParameter(rawVariable, Kotlin.StringClass.typeName(true)) {
                    // TODO: is this still correct, if we support different content-types for a body?
                    addConsumesAnnotation(body.contentTypes)
                }

                val jsonVariable = rawVariable.invoke("parseAsJson".rawMethodName(), "request.body".literal(), "objectMapper".variableName())
                    .assignment()
//
//                kotlinStatement {
//                    write("val node = body.parseAsJson(\"request.body\", objectMapper)")
//                }
//                val parameter = "body".variableName().extend(postfix = "maybe")
//                val source = "node".variableName().pathExpression()
//                addStatement(getDeserializationStatement(source, parameter, it, true))
//                statement.addParameter(parameter)

            }
//            val statement = RequestBuilderDeserializationStatement(
//                request.name, request.name.extend(postfix = "Request").className()
//            )
//
//
//
//            addStatement(statement)
        }
    }
}