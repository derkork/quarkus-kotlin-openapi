package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies in which direction a schema is used. Only in the request from client to server (UnidirectionalUp), only
// in the response from the server back to the client (UnidirectionalDown), or in both directions (Bidirectional)
object SchemaDirectionHint : Hint<SchemaDirection> {

    val OpenApiSchema.schemaDirection: SchemaDirection
        get() = get(SchemaDirectionHint) ?: ProbableBug("Schema direction not set")

    fun OpenApiSchema.hasSchemaDirection() = has(SchemaDirectionHint)
    
    fun OpenApiSchema.hasSchemaDirection(direction: SchemaDirection) = get(SchemaDirectionHint) == direction

    /**
     * returns whether the given direction changed something or not
     */
    fun OpenApiSchema.addSchemaDirection(direction: Direction): Boolean {
        val requestedSchemaDirection = when (direction) {
            Direction.Up -> SchemaDirection.UnidirectionalUp
            Direction.Down -> SchemaDirection.UnidirectionalDown
        }

        val existingSchemaDirection = get(SchemaDirectionHint)

        if (existingSchemaDirection == SchemaDirection.Bidirectional) {
            // nothing to do, as this already covers this direction
            return false
        }

        if (existingSchemaDirection == null) {
            // just apply the new value
            set(SchemaDirectionHint, requestedSchemaDirection)
            return true
        }

        if (existingSchemaDirection != requestedSchemaDirection) {
            // switch from unidirectional to bidirectional
            set(SchemaDirectionHint, SchemaDirection.Bidirectional)
            return true
        }

        // nothing changed, as this was the same direction again
        return false
    }

}

enum class SchemaDirection {

    // flow only from client to server in the request
    UnidirectionalUp,

    // flow only from server back to the client in the response
    UnidirectionalDown,

    // in request and response
    Bidirectional

}