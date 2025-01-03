package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

class ModelNameComponent(val value: String) : SchemaComponent, MetaComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (_, remainingComponents) = other.partitionIsInstance<ModelNameComponent>()

        // just ignore other names
        return this to remainingComponents
    }

}