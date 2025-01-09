package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.NameSuggestionHint.nameSuggestion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestIdentifierHint.requestIdentifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaNameHint.hasSchemaName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaNameHint.schemaName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TransformationStrategy
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

class SchemaNameRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // first step: preparation phase. If a schema has a model name (specified by the developer via 'x-model-name'
        // or a name suggestion (because its available as a component via $ref) use this
        spec.schemas.forEach { assignPredefinedName(it) }

        // second step: name every schema used directly by a request or response if no name is set yet
        spec.inspect {
            bundles {
                requests {
                    val requestPrefix = request.requestIdentifier
                    parameters {
                        assignName(
                            parameter.content.schema,
                            parameter.nameSuggestion,
                            SchemaName("$requestPrefix ${parameter.name} parameter")
                        )
                    }

                    body {
                        assignName(body.content.schema, body.nameSuggestion, SchemaName("$requestPrefix body"))
                    }

                    responses {
                        val responsePrefix = "$requestPrefix ${response.responseCode.asLabel()}"
                        headers {
                            assignName(
                                header.content.schema,
                                header.nameSuggestion,
                                SchemaName("$responsePrefix ${header.name} header")
                            )
                        }

                        body {
                            // the nameSuggestion hint is available at the response and not the body. This is
                            // a different behaviour as for the body of a request, because a request body can be
                            // referenced, but the body of a response can't. Only the response including its body
                            // can be referenced
                            assignName(
                                body.content.schema,
                                response.nameSuggestion,
                                SchemaName("$responsePrefix response")
                            )
                        }
                    }
                }
            }
        }

        // third step: assign names to every schema used within other schema if no name is yet set
        // we start with already named schemas
        val definitionsWithNames = spec.schemas.filterNot { !it.hasSchemaName() }.toMutableSet()

        // helper function to avoid code duplication
        val assignAndSchedule: (OpenApiSchema, fallback: SchemaName) -> Unit = { schema, name ->
            if (assignName(schema, null, name)) {
                // if a name was given to the schema, add it to the set to check its sub schemas too
                definitionsWithNames.add(schema)
            }
        }

        // we can ignore BaseDefinitionComponents here, because these schemas should already have been assigned a name
        // in the first step
        while (definitionsWithNames.isNotEmpty()) {
            definitionsWithNames.pop { current ->
                current.inspect {
                    val name = schema.schemaName
                    components<ArrayItemsComponent> { assignAndSchedule(component.schema, name.postfix("items")) }
                    components<ObjectComponent> {
                        component.properties.forEach { assignAndSchedule(it.schema, name.postfix(it.name)) }
                    }
                    components<MapComponent> { assignAndSchedule(component.schema, name.postfix("value")) }
                    components<SomeOfComponent> {
                        component.options.forEachIndexed { index, option ->
                            assignAndSchedule(option.schema, name.postfix("option").postfix("${index + 1}"))
                        }
                    }
                }
            }
        }

        if (spec.schemas.any { !it.hasSchemaName() }) {
            ProbableBug("Could not assign names to some schemas")
        }
    }

    private fun assignPredefinedName(schema: OpenApiSchema) {
        if (schema.hasSchemaName()) {
            // already a name assigned
            return
        }

        val modelName = schema.getComponent<ModelNameComponent>()?.value
        val schemaNameSuggestion = schema.nameSuggestion

        when {
            !modelName.isNullOrBlank() -> schema.schemaName = SchemaName(modelName, TransformationStrategy.Requested)
            !schemaNameSuggestion.isNullOrBlank() -> schema.schemaName = SchemaName(schemaNameSuggestion)
        }
    }

    private fun assignName(schema: OpenApiSchema, componentNameSuggestion: String?, fallback: SchemaName): Boolean {
        if (schema.hasSchemaName()) {
            // already a name assigned
            return false
        }
        schema.schemaName = when (componentNameSuggestion) {
            null -> fallback
            else -> SchemaName(componentNameSuggestion)
        }

        return true
    }

    private fun ResponseCode.asLabel() = when (this) {
        ResponseCode.Default -> "Default"
        is ResponseCode.HttpStatusCode -> "$value"
    }

}