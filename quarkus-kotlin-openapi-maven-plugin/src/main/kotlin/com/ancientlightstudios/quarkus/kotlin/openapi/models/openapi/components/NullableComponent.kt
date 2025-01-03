package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

class NullableComponent(val nullable: Boolean) : SchemaComponent, MetaComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<NullableComponent>()

        // if there is any component where nullable is set to true, it wins
        val isNullable = nullable || otherMergeComponents.any { it.nullable }

        return NullableComponent(isNullable) to remainingComponents
    }
    
}