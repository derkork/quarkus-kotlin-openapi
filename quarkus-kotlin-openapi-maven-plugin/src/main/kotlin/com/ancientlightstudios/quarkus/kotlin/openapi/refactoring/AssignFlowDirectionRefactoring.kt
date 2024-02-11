package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

// apply flow information (up/down usage)
class AssignFlowDirectionRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters { propagate(parameter.typeDefinition, Direction.Up) }

                    body { propagate(body.content.typeDefinition, Direction.Up) }

                    responses {
                        headers { propagate(header.typeDefinition, Direction.Down) }

                        body { propagate(body.content.typeDefinition, Direction.Down) }
                    }
                }
            }
        }
    }

    private fun propagate(typeDefinition: TypeDefinition, direction: Direction) {
        if (typeDefinition.addDirection(direction)) {
            // this direction was not set to the type yet. propagate it down
            when (typeDefinition) {
                // leaf types. nothing to do here
                is PrimitiveTypeDefinition,
                is EnumTypeDefinition -> return

                is CollectionTypeDefinition -> propagate(typeDefinition.items.typeDefinition, direction)
                is ObjectTypeDefinition -> typeDefinition.properties.forEach {
                    propagate(it.schema.typeDefinition, direction)
                }
            }
        }
    }

}
