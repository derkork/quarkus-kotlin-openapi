package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BasedOnHint.basedOn
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.BaseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.replaceWith
import org.slf4j.LoggerFactory

class InlineSchemaComponentsRefactoring : SpecRefactoring {

    private val log = LoggerFactory.getLogger(InlineSchemaComponentsRefactoring::class.java)

    override fun RefactoringContext.perform() {
        var remainingSchemas = spec.schemas

        do {
            val (ready, notReady) = remainingSchemas.partition(::isSchemaReady)
            remainingSchemas = notReady

            if (ready.isEmpty()) {
                // there are no more schemas to process
                break
            }

            ready.forEach(::processSchema)
        } while (true)

        if (remainingSchemas.isNotEmpty()) {
            log.warn("Components for the following schemas can't be processed")
            remainingSchemas.forEach {
                log.warn("- ${it.originPath}")
            }

            ProbableBug("Can't process components for some schemas")
        }
    }

    private fun isSchemaReady(schema: OpenApiSchema): Boolean {
        // schema is ready if there is no base schema component attached, or the base schema has no more dependencies itself
        return schema.components
            .filterIsInstance<BaseSchemaComponent>()
            .none { it.schema.hasComponent<BaseSchemaComponent>() }
    }

    private fun processSchema(schema: OpenApiSchema) {
        // they are required later again to find out the schema kind
        val baseRefs = schema.getComponents<BaseSchemaComponent>()

        // inline every parent component
        baseRefs.forEach { flattenReference(schema, it) }

        // merge everything together so we only have a single component of each type
        mergeComponents(schema)

        schema.basedOn = baseRefs.map { it.schema }
    }

    private fun flattenReference(schema: OpenApiSchema, ref: BaseSchemaComponent) {
        schema.components = schema.components.replaceWith(ref, *ref.schema.components.toTypedArray())
    }

    private fun mergeComponents(schema: OpenApiSchema) {
        val finalComponents = mutableListOf<SchemaComponent>()
        // all the components which still have to be visited
        var remainingComponents = schema.components

        do {
            // take the first component, or leave if there is nothing else to do
            val firstComponent = remainingComponents.firstOrNull() ?: break

            // everything except the first component we just picked
            val otherComponents = remainingComponents.drop(1)
            val (mergedComponent, unmergedComponents) = firstComponent.merge(otherComponents, schema.originPath)
            finalComponents.add(mergedComponent)
            remainingComponents = unmergedComponents
        } while (true)

        if (remainingComponents.isNotEmpty()) {
            ProbableBug("Can't merge components for schema ${schema.originPath}")
        }

        schema.components = finalComponents
    }
}