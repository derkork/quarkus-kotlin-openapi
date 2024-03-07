package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation

sealed interface TypeDefinition {

    val nullable: Boolean

    val directions: Set<Direction>

    val validations: List<SchemaValidation>

    fun addContentType(direction: Direction, contentType: ContentType): Boolean

    fun getContentTypes(direction: Direction): Set<ContentType>

}

// this is just a marker interface, so the code generator knows which type definitions can be ignored
interface TypeDefinitionOverlay

enum class Direction {
    // data from client to server
    Up,

    // data from server to client
    Down
}