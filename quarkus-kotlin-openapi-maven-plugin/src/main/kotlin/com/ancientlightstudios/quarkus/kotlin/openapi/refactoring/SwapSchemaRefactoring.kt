package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*

class SwapSchemaRefactoring(
    private val current: OpenApiSchema,
    private val replacement: OpenApiSchema
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