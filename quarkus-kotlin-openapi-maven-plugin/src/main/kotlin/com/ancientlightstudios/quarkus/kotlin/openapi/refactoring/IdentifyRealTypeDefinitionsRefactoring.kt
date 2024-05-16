package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ModelTypesHint.modelTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

class IdentifyRealTypeDefinitionsRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        val models = mutableSetOf<TypeDefinition>()

        val tasks = mutableSetOf<TypeDefinition>()
        spec.inspect {
            bundles {
                requests {
                    parameters { tasks.add(unwrapOverlay(parameter.typeUsage)) }

                    body { tasks.add(unwrapOverlay(body.content.typeUsage)) }

                    responses {
                        headers { tasks.add(unwrapOverlay(header.typeUsage)) }

                        body { tasks.add(unwrapOverlay(body.content.typeUsage)) }
                    }
                }
            }
        }

        while (tasks.isNotEmpty()) {
            tasks.pop {
                // check if we already have this type and can skip it
                if (!models.add(it)) {
                    return@pop
                }

                when (it) {
                    is PrimitiveTypeDefinition,
                    is EnumTypeDefinition -> return@pop

                    is ObjectTypeDefinition -> it.properties.mapTo(tasks) { unwrapOverlay(it.typeUsage) }
                    is CollectionTypeDefinition -> tasks.add(unwrapOverlay(it.items))
                    is OneOfTypeDefinition -> it.options.mapTo(tasks) { unwrapOverlay(it.typeUsage) }
                }
            }
        }

        spec.modelTypes = models.toList()
    }

    private fun unwrapOverlay(typeUsage: TypeUsage): TypeDefinition {
        var current = typeUsage.type
        while (current is TypeDefinitionOverlay) {
            current = current.base
        }
        return current
    }

}
