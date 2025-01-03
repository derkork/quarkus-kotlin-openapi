package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Library
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContainer

class ServerRequestContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerRequestContainer>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(container: ServerRequestContainer) {
        kotlinFile(container.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinClass(name) {
            }
        }
    }
}

//    private fun RequestInspection.emitContainerFile() = kotlinFile(request.requestContainerClassName) {
//        kotlinClass(fileName) {
//            parameters {
//                val typeUsage = parameter.content.typeUsage
//                kotlinMember(
//                    parameter.parameterVariableName, typeUsage.buildValidType(),
//                    accessModifier = null
//                )
//            }
//
//            body {
//                val typeUsage = body.content.typeUsage
//                kotlinMember(
//                    body.parameterVariableName, typeUsage.buildValidType(), accessModifier = null
//                )
//            }
//        }
//    }
//
//}