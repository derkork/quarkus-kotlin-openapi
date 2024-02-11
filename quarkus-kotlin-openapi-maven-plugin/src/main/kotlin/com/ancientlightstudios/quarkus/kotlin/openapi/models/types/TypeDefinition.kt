package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.CustomConstraintsValidationComponent

sealed interface TypeDefinition {

    val nullable: Boolean

    val contentTypes: Set<ContentType>

    val directions: Set<Direction>

    val customConstraints: List<CustomConstraintsValidationComponent>

    fun addContentType(contentType: ContentType): Boolean

    fun addDirection(direction: Direction): Boolean

}

// this is just a marker interface, so the code generator knows which type definitions can be ignored
interface TypeDefinitionOverlay

enum class Direction {
    // data from client to server
    Up,

    // data from server to client
    Down
}