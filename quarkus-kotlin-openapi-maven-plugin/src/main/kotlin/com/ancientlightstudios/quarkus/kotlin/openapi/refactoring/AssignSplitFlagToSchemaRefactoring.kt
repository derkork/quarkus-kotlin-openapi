package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OverlayTargetHint.overlayTarget
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirectionHint.hasSchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaModeHint.hasSchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModelHint.schemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SplitFlagHint.setSplitFlag
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*

class AssignSplitFlagToSchemaRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // only bidirectional schemas might need the split flag, so we can ignore everything else
        val bidirectionalSchemas = spec.schemas.filter { it.hasSchemaDirection(SchemaDirection.Bidirectional) }

        // first identify our starting set
        val startingSet = bidirectionalSchemas
            // schema which is an object model (but not an overlay)
            .filter { it.hasSchemaMode(SchemaMode.Model) && it.schemaTargetModel == SchemaTargetModel.ObjectModel }
            // and has at least one read-only or write-only property
            .filter {
                val properties = it.getComponent<ObjectComponent>()?.properties ?: return@filter false

                properties.any { property -> property.schema.hasComponent<SchemaModifierComponent>() }
            }

        // assign the split flag hint to them
        startingSet.forEach {
            it.setSplitFlag()
        }

        // all the schemas we already marked with the split flag and don't need to be processed again
        val visited = startingSet.toMutableSet()

        // helper function to avoid code duplication
        val checkAndSchedule: (current: OpenApiSchema, nested: OpenApiSchema, batch: MutableSet<OpenApiSchema>) -> Unit =
            { current, nested, batch ->
                // the nested schema was marked with the split flag, but it's the first time we see the current schema
                // add it to the batch so it will receive the flag and will be added to the visited set so other schemas
                // can check their dependencies
                if (!batch.contains(current) && visited.contains(nested)) {
                    batch.add(current)
                }
            }

        do {
            // every bidirectional schema, which is not yet marked with the flag
            val candidates = bidirectionalSchemas.subtract(visited)

            if (candidates.isEmpty()) {
                // there is no schema left, we can stop now
                break
            }

            // all the schemas which just received the split flag and need to be processed
            val nextBatch = mutableSetOf<OpenApiSchema>()

            candidates.forEach { schema ->
                schema.inspect {
                    components<AnyOfComponent> {
                        component.options.forEach { checkAndSchedule(schema, it.schema, nextBatch) }
                    }
                    components<ArrayItemsComponent> { checkAndSchedule(schema, component.schema, nextBatch) }
                    components<MapComponent> { checkAndSchedule(schema, component.schema, nextBatch) }
                    components<ObjectComponent> {
                        component.properties.forEach { checkAndSchedule(schema, it.schema, nextBatch) }
                    }
                    components<OneOfComponent> {
                        component.options.forEach { checkAndSchedule(schema, it.schema, nextBatch) }
                    }
                }

                // if this schema is an overlay, check if the target model was marked with the split flag. If it is a
                // model, there is nothing else to do, because a model is always a final schema
                if (schema.hasSchemaMode(SchemaMode.Overlay)) {
                    checkAndSchedule(schema, schema.overlayTarget, nextBatch)
                }
            }

            if (nextBatch.isEmpty()) {
                // nothing was changed, so we can stop now
                break
            }

            // assign the split flag
            nextBatch.forEach {
                it.setSplitFlag()
            }

            // add the new schemas to the visited set, so other schemas can now check their dependencies
            visited.addAll(nextBatch)
        } while (true)
    }

}