package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

interface SchemaComponent {

    // TODO: add the schema origin path as a parameter so we can generate useful error messages

    // a list with other components to select merge candidates from. The list must not contain the current component
    // itself. Returns a pair with the first parameter the merged component and the second parameter all remaining
    // (incompatible) components from the given list
    fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>>

}

inline fun <reified T : SchemaComponent> List<SchemaComponent>.partitionIsInstance(): Pair<List<T>, List<SchemaComponent>> {
    val (similar, notSimilar) = this.partition { it is T }

    @Suppress("UNCHECKED_CAST")
    return (similar as List<T>) to notSimilar
}

