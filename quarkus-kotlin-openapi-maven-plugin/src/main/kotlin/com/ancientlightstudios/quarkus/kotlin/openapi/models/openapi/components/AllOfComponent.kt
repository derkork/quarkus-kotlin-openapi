package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class AllOfComponent(override val options: List<SomeOfOption>) : SomeOfComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> =
        ProbableBug("allOf components should no longer be available when schema components are merged. Found in schema $origin")

}
