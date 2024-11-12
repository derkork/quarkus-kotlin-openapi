package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.ArrayItemsComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.MapComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.ObjectComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SomeOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

class SchemaNameRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // first step: name every schema used directly by a request or response if no name is set yet
        spec.inspect {
            bundles {
                requests {
                    val requestPrefix = request.operationId
                    parameters {
                        assignName(
                            parameter.content.schema,
                            parameter.nameSuggestion,
                            "$requestPrefix ${parameter.name} parameter"
                        )
                    }

                    body {
                        assignName(body.content.schema, body.nameSuggestion, "$requestPrefix body")
                    }

                    responses {
                        val responsePrefix = "$requestPrefix ${response.responseCode}"
                        headers {
                            assignName(
                                header.content.schema,
                                header.nameSuggestion,
                                "$responsePrefix ${header.name} header"
                            )
                        }

                        body {
                            assignName(body.content.schema, body.nameSuggestion, "$responsePrefix response")
                        }
                    }
                }
            }
        }

        // second step: assign names to every schema used within other schema if no name is yet set
        // we start with already named schemas
        val definitionsWithNames = spec.schemas.filterNot { it.name.isBlank() }.toMutableSet()

        // helper function to avoid code duplication
        val assignAndSchedule: (OpenApiSchema, fallback: String) -> Unit = { schema, name ->
            if (assignName(schema, null, name)) {
                // if a name was given to the schema, add it to the set to check its sub schemas too
                definitionsWithNames.add(schema)
            }
        }

        // we can ignore BaseDefinitionComponents here, because these schemas should already have a name based on the reference
        while (definitionsWithNames.isNotEmpty()) {
            definitionsWithNames.pop { current ->
                current.inspect {
                    val prefix = schema.name
                    components<ArrayItemsComponent> { assignAndSchedule(component.schema, "$prefix items") }
                    components<ObjectComponent> {
                        component.properties.forEach {
                            assignAndSchedule(it.schema, "$prefix ${it.name}")
                        }
                    }
                    components<MapComponent> { assignAndSchedule(component.schema, "$prefix value") }
                    components<SomeOfComponent> {
                        component.schemas.forEachIndexed { index, schema ->
                            assignAndSchedule(schema.schema, "$prefix option ${index + 1}")
                        }
                    }
                }
            }
        }

        if (spec.schemas.any { it.name.isBlank() }) {
            ProbableBug("Could not assign names to some schemas")
        }
    }

    private fun assignName(schema: OpenApiSchema, suggestion: String?, fallback: String): Boolean {
        if (schema.name.isNotBlank()) {
            // already a name assigned
            return false
        }

        schema.name = suggestion ?: fallback
        return true
    }
}