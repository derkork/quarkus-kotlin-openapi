package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.CustomConstraintsValidationComponent

sealed interface TypeDefinition {

    val nullable: Boolean

    val contentTypes: Set<ContentType>

    val directions: Set<Direction>

    val customConstraints: List<CustomConstraintsValidationComponent>

    fun addContentType(contentType: ContentType) : Boolean

    fun addContentTypes(contentTypes: Collection<ContentType>): Boolean

    fun addDirection(direction: Direction): Boolean

    fun addDirections(directions: Collection<Direction>): Boolean

}

// this is just a marker interface, so the code generator knows which type definitions can be ignored
interface TypeDefinitionOverlay