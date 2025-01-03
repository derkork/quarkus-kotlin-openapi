package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*

// if a schema only contains a BaseSchemaComponent, we can replace it with the inner schema
class ReplaceForwardSchemaRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemas {
                // if there is more than one component, we can't handle it here
                if (schema.components.size != 1) {
                    return@schemas
                }

                val base = schema.getComponent<BaseSchemaComponent>() ?: return@schemas

                spec.schemas -= schema
                replace(schema, base.schema)
            }
        }
    }

    private fun RefactoringContext.replace(schema: OpenApiSchema, replacement: OpenApiSchema) {
        // helper function to avoid code duplication
        val checkAndReplace: (SchemaContainer) -> Unit = { container ->
            if (container.schema == schema) {
                container.schema = replacement
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
                components<SomeOfComponent> { component.options.forEach { checkAndReplace(it) } }
            }
        }
    }
}