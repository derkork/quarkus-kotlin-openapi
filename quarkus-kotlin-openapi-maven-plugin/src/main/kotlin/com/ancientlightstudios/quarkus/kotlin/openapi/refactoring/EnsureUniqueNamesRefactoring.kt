package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientDelegateClassNameHint.clientDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientErrorResponseClassNameHint.clientErrorResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientHttpResponseClassNameHint.clientHttpResponseClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ClientRestInterfaceClassNameHint.clientRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ModelTypesHint.modelTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestBuilderClassNameHint.requestBuilderClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContextClassNameHint.requestContextClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseContainerClassNameHint.responseContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseInterfaceNameHint.responseInterfaceName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ResponseValidatorClassNameHint.responseValidatorClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerDelegateClassNameHint.serverDelegateClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ServerRestInterfaceClassNameHint.serverRestInterfaceClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class EnsureUniqueNamesRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        val registry = NameRegistry()
        spec.inspect {
            bundles {
                bundle.clientDelegateClassName = registry.uniqueNameFor(bundle.clientDelegateClassName)
                bundle.clientRestInterfaceClassName = registry.uniqueNameFor(bundle.clientRestInterfaceClassName)
                bundle.serverDelegateClassName = registry.uniqueNameFor(bundle.serverDelegateClassName)
                bundle.serverRestInterfaceClassName = registry.uniqueNameFor(bundle.serverRestInterfaceClassName)

                requests {
                    request.clientErrorResponseClassName = registry.uniqueNameFor(request.clientErrorResponseClassName)
                    request.clientHttpResponseClassName = registry.uniqueNameFor(request.clientHttpResponseClassName)
                    request.requestContainerClassName = registry.uniqueNameFor(request.requestContainerClassName)
                    request.requestContextClassName = registry.uniqueNameFor(request.requestContextClassName)
                    request.requestBuilderClassName = registry.uniqueNameFor(request.requestBuilderClassName)
                    request.responseContainerClassName = registry.uniqueNameFor(request.responseContainerClassName)
                    request.responseValidatorClassName = registry.uniqueNameFor(request.responseValidatorClassName)

                    responses {
                        response.responseInterfaceName?.let {
                            response.responseInterfaceName = registry.uniqueNameFor(it, true)
                        }
                    }
                }
            }
        }

        spec.modelTypes
            .filterNot { it is TypeDefinitionOverlay }
            .forEach {
                when (it) {
                    is PrimitiveTypeDefinition,
                    is CollectionTypeDefinition -> return@forEach

                    // this cast is safe, because overlays were filtered out before
                    is EnumTypeDefinition ->
                        (it as RealEnumTypeDefinition).modelName = registry.uniqueNameFor(it.modelName)

                    is ObjectTypeDefinition ->
                        (it as RealObjectTypeDefinition).modelName = registry.uniqueNameFor(it.modelName)

                    is OneOfTypeDefinition -> {
                        (it as RealOneOfTypeDefinition).modelName = registry.uniqueNameFor(it.modelName)
                        it.options.forEach {
                            it.modelName = registry.uniqueNameFor(it.modelName)
                        }
                    }
                }
            }
    }

}

class NameRegistry {

    private val nameBuilder = mutableMapOf<String, NameBuilder>()

    fun uniqueNameFor(name: ClassName, shared: Boolean = false): ClassName {
        val builder = nameBuilder.getOrPut(name.value) { NameBuilder() }
        return when (shared) {
            true -> builder.shared(name)
            false -> builder.next(name)
        }
    }

     private class NameBuilder {

        private var nextIndex = -1
        private var shared = -1

        fun next(name: ClassName) = foo(name, ++nextIndex)

        fun shared(name: ClassName): ClassName {
            if (shared == -1) {
                // it's the first time, we need the shared class name. freeze the index and reuse it from now on
                shared = ++nextIndex
            }
            return foo(name, shared)
        }

        private fun foo(name: ClassName, index: Int): ClassName {
            if (index == 0) {
                return name
            }
            return name.extend(postfix = "$index")
        }

    }
}