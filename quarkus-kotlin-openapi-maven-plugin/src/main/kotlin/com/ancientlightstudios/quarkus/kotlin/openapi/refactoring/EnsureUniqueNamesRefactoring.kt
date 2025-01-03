package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

@Deprecated(message = "")
class EnsureUniqueNamesRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
/*
        val registry = NameRegistry()

        spec.modelTypes
            .filterNot { it is TypeDefinitionOverlay }
            .forEach {
                when (it) {
                    is PrimitiveTypeDefinition,
                    is CollectionTypeDefinition -> return@forEach

                    // this cast is safe, because overlays were filtered out before
                    is EnumTypeDefinition ->
                        (it as RealEnumTypeDefinition).modelName = registry.uniqueNameFor(it.modelName)

                    is ObjectTypeDefinition -> {
                        if (!it.isPureMap) {
                            (it as RealObjectTypeDefinition).modelName = registry.uniqueNameFor(it.modelName)
                        }
                        // nothing to do here for a pure map
                    }

                    is OneOfTypeDefinition -> {
                        (it as RealOneOfTypeDefinition).modelName = registry.uniqueNameFor(it.modelName)
                        it.options.forEach {
                            it.modelName = registry.uniqueNameFor(it.modelName)
                        }
                    }
                }
            }

 */
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