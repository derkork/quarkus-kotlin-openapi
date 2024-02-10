package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaDefinitionComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class TransformableSchemaDefinition(
    var name: String,
    var components: List<SchemaDefinitionComponent> = listOf()
) : TransformableObject() {

    inline  fun <reified T: SchemaDefinitionComponent> getComponent() : T? {
        val result = components.filterIsInstance<T>()
        return when {
            result.isEmpty() -> null
            result.size > 1 -> ProbableBug("Multiple instances of the same component found at schema definition $originPath")
            else -> result.first()
        }
    }

}



