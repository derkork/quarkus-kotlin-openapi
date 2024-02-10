package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

class SchemaDefinitionNameRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // first step: name every schema used directly by a request or response if no name is set yet
        spec.inspect {
            bundles {
                requests {
                    val requestPrefix = request.operationId
                    parameters {
                        assignName(
                            parameter.schema,
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
                                header.schema,
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

        // second step: assign names to every schema used within other schema if no name is set yet
        // we start with already named schemas
        val definitionsWithNames = spec.schemaDefinitions.filterNot { it.name.isBlank() }.toMutableSet()

        // helper function to avoid code duplication
        val assignAndSchedule: (TransformableSchemaUsage, fallback: String) -> Unit = { schema, name ->
            if (assignName(schema, null, name)) {
                // if a name was given to the schema, add it to the set to check its sub schemas too
                definitionsWithNames.add(schema.schemaDefinition)
            }
        }

        // we can ignore BaseDefinitionComponents here, because these schemas should already have a name based on the reference
        while (definitionsWithNames.isNotEmpty()) {
            definitionsWithNames.pop { current ->
                current.inspect {
                    val prefix = schemaDefinition.name
                    components<ArrayItemsComponent> { assignAndSchedule(component.itemsSchema, "$prefix items") }
                    components<ObjectComponent> {
                        component.properties.forEach {
                            assignAndSchedule(it.schema, "$prefix ${it.name}")
                        }
                    }
                    components<AllOfComponent> {
                        component.schemas.forEachIndexed { index, schema ->
                            assignAndSchedule(schema, "$prefix option ${index + 1}")
                        }
                    }
                    components<AnyOfComponent> {
                        component.schemas.forEachIndexed { index, schema ->
                            assignAndSchedule(schema, "$prefix option ${index + 1}")
                        }
                    }
                    components<OneOfComponent> {
                        component.schemas.forEachIndexed { index, schema ->
                            assignAndSchedule(schema, "$prefix option ${index + 1}")
                        }
                    }
                }
            }
        }

        if (spec.schemaDefinitions.any { it.name.isBlank() }) {
            ProbableBug("Could not assign names to some schemas")
        }
    }

    private fun assignName(usage: TransformableSchemaUsage, suggestion: String?, fallback: String): Boolean {
        val schemaDefinition = usage.schemaDefinition
        if (schemaDefinition.name.isNotBlank()) {
            // already a name assigned
            return false
        }

        schemaDefinition.name = suggestion ?: fallback
        return true
    }
}