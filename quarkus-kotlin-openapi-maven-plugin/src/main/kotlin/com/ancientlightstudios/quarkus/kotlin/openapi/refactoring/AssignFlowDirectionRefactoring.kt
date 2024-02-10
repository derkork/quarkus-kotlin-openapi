package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DirectionHint.addDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DirectionHint.directions
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

// apply flow information (up/down usage)
class AssignFlowDirectionRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        val tasks = mutableSetOf<TransformableSchemaDefinition>()

        // helper function to avoid code duplication
        val perform: (TransformableSchemaUsage, Direction) -> Unit = { usage, direction ->
            usage.addDirection(direction)
            // only if it was not yet added to the definition, we have to propagate it down later
            if (usage.schemaDefinition.addDirection(direction)) {
                tasks.add(usage.schemaDefinition)
            }
        }

        spec.inspect {
            bundles {
                requests {
                    parameters { perform(parameter.schema, Direction.Up) }

                    body { perform(body.content.schema, Direction.Up) }

                    responses {
                        headers { perform(header.schema, Direction.Down) }

                        body { perform(body.content.schema, Direction.Down) }
                    }
                }
            }
        }

        // now propagate the direction to any sub schema as they are used the same way too
        while (tasks.isNotEmpty()) {
            tasks.pop { current ->
                current.inspect {
                    val myDirections = schemaDefinition.directions
                    nestedSchemas {
                        usage.addDirection(*myDirections.toTypedArray())
                        // same as above, only if it was not yet added to the definition propagate it down
                        if (usage.schemaDefinition.addDirection(*myDirections.toTypedArray())) {
                            tasks.add(usage.schemaDefinition)
                        }
                    }
                }
            }
        }
    }

}
