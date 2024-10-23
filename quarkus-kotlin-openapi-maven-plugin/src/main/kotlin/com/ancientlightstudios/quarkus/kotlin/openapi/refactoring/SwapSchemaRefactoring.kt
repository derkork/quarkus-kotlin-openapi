package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*

class SwapSchemaRefactoring(
    private val current: TransformableSchema,
    private val replacement: TransformableSchema
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // helper function to avoid code duplication
        val checkAndReplace: (SchemaUsage) -> Unit = { schemaUsage ->
            if (schemaUsage.schema == current) {
                schemaUsage.schema = replacement
            }
        }

        spec.inspect {
            bundles {
                requests {
                    parameters { checkAndReplace(parameter.content) }

                    body { checkAndReplace(body.content) }

                    responses {
                        headers { checkAndReplace(header.content) }

                        body { checkAndReplace(body.content) }
                    }
                }
            }

            schemas {
                components<BaseSchemaComponent> { checkAndReplace(component) }
                components<ArrayItemsComponent> { checkAndReplace(component) }
                components<ObjectComponent> { component.properties.forEach { checkAndReplace(it) } }
                components<MapComponent> { checkAndReplace(component) }
                components<SomeOfComponent> { component.schemas.forEach { checkAndReplace(it) } }
            }
        }
    }

}