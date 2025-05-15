package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class MapComponent(override var schema: OpenApiSchema) : SchemaComponent, SchemaContainer, StructuralComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<MapComponent>()

        val hasIncompatibleItems = otherMergeComponents.any { it.schema.originPath != schema.originPath }
        if (hasIncompatibleItems) {
            SpecIssue("different map items detected. Found in schema $origin")
        }

        return this to remainingComponents
    }

}
