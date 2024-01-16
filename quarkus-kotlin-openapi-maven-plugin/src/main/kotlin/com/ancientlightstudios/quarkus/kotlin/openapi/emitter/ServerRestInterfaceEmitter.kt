package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestBundleInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestNameHint.requestName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateNameHint.serverDelegateName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceNameHint.serverRestInterfaceName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
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

    private fun RequestBundleInspection.emitRestInterfaceFile() = kotlinFile(bundle.serverRestInterfaceName) {
        kotlinClass(fileName) {
            addPathAnnotation(pathPrefix)

            kotlinMember("delegate".variableName(), bundle.serverDelegateName.typeName())
            kotlinMember("objectMapper".variableName(), Misc.ObjectMapperClass.typeName())

            requests {
                emitRequest(this@kotlinClass)
            }
        }
    }

    private fun RequestInspection.emitRequest(containerClass: KotlinClass) = with(containerClass) {
        kotlinMethod(request.requestName, true, Misc.RestResponseClass.typeName().of(Kotlin.Star)) {
            addRequestMethodAnnotation(request.method)
            addPathAnnotation(request.path)

//            if (request.output.responses.any { it.type != null }) {
//                kotlinAnnotation(
//                    "Produces".rawClassName(),
//                    "value".variableName() to "application/json".literal()
//                )
//            }

//            val statement = RequestBuilderDeserializationStatement(
//                request.name, request.name.extend(postfix = "Request").className()
//            )
//
//            request.input.forEach {
//                kotlinParameter(it.name.variableName(), it.type.unsafeType) {
//                    kotlinAnnotation(
//                        it.source.value.rawClassName(),
//                        "value".variableName() to it.name.stringExpression()
//                    )
//                }
//                val parameter = it.name.variableName().extend(postfix = "maybe")
//                val source = it.name.variableName().parameterToMaybeExpression(
//                    "request.${it.source.name.lowercase()}.${it.name}".stringExpression()
//                )
//                addStatement(getDeserializationStatement(source, parameter, it.type, false))
//                statement.addParameter(parameter)
//            }
//
//            request.body?.let {
//                kotlinParameter("body".variableName(), "String".rawTypeName(true))
//                kotlinAnnotation(
//                    "Consumes".rawClassName(),
//                    "value".variableName() to "application/json".stringExpression().arrayExpression()
//                )
//
//                kotlinStatement {
//                    write("val node = body.parseAsJson(\"request.body\", objectMapper)")
//                }
//                val parameter = "body".variableName().extend(postfix = "maybe")
//                val source = "node".variableName().pathExpression()
//                addStatement(getDeserializationStatement(source, parameter, it, true))
//                statement.addParameter(parameter)
//            }
//
//            addStatement(statement)
        }
    }
}