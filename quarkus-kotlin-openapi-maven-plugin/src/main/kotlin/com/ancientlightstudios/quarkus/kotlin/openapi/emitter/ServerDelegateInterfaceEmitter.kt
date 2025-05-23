package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerDelegateInterface

class ServerDelegateInterfaceEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ServerDelegateInterface>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(delegateInterface: ServerDelegateInterface) {
        kotlinFile(delegateInterface.name.asTypeName()) {
            registerImports(Library.All)

            kotlinInterface(name) {
                delegateInterface.methods.forEach { interfaceMethod ->
                    kotlinMethod(
                        interfaceMethod.name, true, Kotlin.Nothing.asTypeReference(),
                        interfaceMethod.receiver.name.asTypeReference()
                    )
                }
            }
        }
    }

}
