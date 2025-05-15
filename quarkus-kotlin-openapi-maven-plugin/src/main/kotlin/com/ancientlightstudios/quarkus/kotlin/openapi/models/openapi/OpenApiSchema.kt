package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class OpenApiSchema(
    var components: List<SchemaComponent> = listOf()
) : HintsAware() {

    inline fun <reified T : SchemaComponent> hasComponent(): Boolean = components.any { it is T }

    @JvmName("getComponentsOfType")
    inline fun <reified T : SchemaComponent> getComponents(): List<T> = components.filterIsInstance<T>()

    inline fun <reified T : SchemaComponent> getComponent(): T? {
        val result = components.filterIsInstance<T>()
        return when {
            result.isEmpty() -> null
            result.size > 1 -> ProbableBug("Multiple instances of the same component found at schema $originPath")
            else -> result.first()
        }
    }
}



