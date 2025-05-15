package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OverlayTargetHint.overlayTarget
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirectionHint.addSchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaModeHint.hasSchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.Direction

class AssignSchemaDirectionRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters {
                        setAndPropagate(parameter.content.schema, Direction.Up)
                    }

                    body { setAndPropagate(body.content.schema, Direction.Up) }

                    responses {
                        headers {
                            setAndPropagate(header.content.schema, Direction.Down)
                        }

                        body {
                            setAndPropagate(body.content.schema, Direction.Down)
                        }
                    }
                }
            }
        }
    }

    private fun setAndPropagate(schema: OpenApiSchema, direction: Direction) {
        if (schema.addSchemaDirection(direction)) {
            // new direction for this schema. propagate it down

            schema.inspect {
                // there are no allOf or baseSchema components left at this point
                components<AnyOfComponent> { component.options.forEach { setAndPropagate(it.schema, direction) } }
                components<ArrayItemsComponent> { setAndPropagate(component.schema, direction) }
                components<MapComponent> { setAndPropagate(component.schema, direction) }
                components<ObjectComponent> { component.properties.forEach { setAndPropagate(it.schema, direction) } }
                components<OneOfComponent> { component.options.forEach { setAndPropagate(it.schema, direction) } }

                // if this schema is an overlay, propagate the direction to its target model too. If it is a
                // model, there is nothing else to do, because a model is always a final schema
                if (schema.hasSchemaMode(SchemaMode.Overlay)) {
                    setAndPropagate(schema.overlayTarget, direction)
                }
            }

        }
    }

}