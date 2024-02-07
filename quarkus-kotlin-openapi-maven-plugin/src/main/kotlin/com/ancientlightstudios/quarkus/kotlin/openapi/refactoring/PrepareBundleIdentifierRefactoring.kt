package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientDelegateClassNameHint.clientDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientRestInterfaceClassNameHint.clientRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceClassNameHint.serverRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle

class PrepareBundleIdentifierRefactoring(private val interfaceName: String) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                val name = generateName(bundle)
                bundle.serverDelegateClassName = name.className(serverPackage, postfix = "Delegate")
                bundle.serverRestInterfaceClassName = name.className(serverPackage, postfix = "Server")
                bundle.clientDelegateClassName = name.className(clientPackage, postfix = "Delegate")
                bundle.clientRestInterfaceClassName = name.className(clientPackage, postfix = "Client")
            }
        }
    }

    private fun generateName(bundle: TransformableRequestBundle): String {
        val tag = bundle.tag?.trim() ?: ""
        return "$interfaceName $tag"
    }

}