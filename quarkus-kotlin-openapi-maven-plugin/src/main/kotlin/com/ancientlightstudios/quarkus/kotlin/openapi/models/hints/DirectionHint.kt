package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage

// specifies in which direction a schema definition is used
object DirectionHint : Hint<MutableSet<Direction>> {

    val TransformableSchemaDefinition.directions: Set<Direction>
        get() = get(DirectionHint) ?: emptySet()

    val TransformableSchemaUsage.directions: Set<Direction>
        get() = get(DirectionHint) ?: emptySet()

    fun TransformableSchemaDefinition.addDirection(vararg direction: Direction): Boolean {
        if (direction.isEmpty()) {
            return false
        }

        val data = getOrPut(DirectionHint) { mutableSetOf() }
        return data.addAll(direction)
    }

    fun TransformableSchemaUsage.addDirection(vararg direction: Direction): Boolean {
        if (direction.isEmpty()) {
            return false
        }

        val data = getOrPut(DirectionHint) { mutableSetOf() }
        return data.addAll(direction)
    }

}

enum class Direction {
    // data from client to server
    Up,

    // data from server to client
    Down
}