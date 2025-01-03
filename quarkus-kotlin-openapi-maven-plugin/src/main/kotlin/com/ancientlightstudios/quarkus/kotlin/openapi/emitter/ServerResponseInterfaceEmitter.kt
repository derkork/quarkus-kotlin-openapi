package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerResponseInterface

class ServerResponseInterfaceEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerResponseInterface>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(responseInterface: ServerResponseInterface) {
        kotlinFile(responseInterface.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinInterface(name) {
                kotlinMethod(responseInterface.methodName, returnType = Kotlin.Nothing.asTypeReference()) {
                    if (responseInterface.responseCode == ResponseCode.Default) {
                        kotlinParameter("status", Kotlin.Int.asTypeReference())
                    }
//                    emitMethodBody(body, headers)
                }
            }
        }
    }
}

//
//    private fun KotlinMethod.emitMethodBody(
//        body: OpenApiBody?,
//        headers: List<OpenApiParameter>
//    ) {
//        if (body != null) {
//            val typeUsage = body.content.typeUsage
//            kotlinParameter("body".variableName(), typeUsage.buildValidType())
//        }
//
//        headers.forEach {
//            kotlinParameter(it.parameterVariableName, it.content.typeUsage.buildValidType())
//        }
//    }
//}