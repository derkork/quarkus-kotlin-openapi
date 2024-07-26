package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class TransformableSchema(
    var name: String,
    var components: List<SchemaComponent> = listOf()
) : TransformableObject() {

    inline fun <reified T : SchemaComponent> getComponent(): T? {
        val result = components.filterIsInstance<T>()
        return when {
            result.isEmpty() -> null
            result.size > 1 -> ProbableBug("Multiple instances of the same component found at schema $originPath")
            else -> result.first()
        }
    }

}



