package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientDelegateNameHint.clientDelegateName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientRestInterfaceNameHint.clientRestInterfaceName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateNameHint.serverDelegateName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceNameHint.serverRestInterfaceName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle

class PrepareBundleIdentifierRefactoring(private val interfaceName: String) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                val name = generateName(bundle)
                bundle.serverDelegateName = name.className(serverPackage, postfix = "Delegate")
                bundle.serverRestInterfaceName = name.className(serverPackage, postfix = "Server")
                bundle.clientDelegateName = name.className(clientPackage, postfix = "Delegate")
                bundle.clientRestInterfaceName = name.className(clientPackage, postfix = "Client")
            }
        }
    }

    private fun generateName(bundle: TransformableRequestBundle): String {
        val tag = bundle.tag?.trim() ?: ""
        return "$interfaceName $tag"
    }

}