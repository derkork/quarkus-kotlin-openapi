package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation

sealed interface TypeDefinition {

    val nullable: Boolean

    val modifier: SchemaModifier?

    val directions: Set<Direction>

    val validations: List<SchemaValidation>

    fun addContentType(direction: Direction, contentType: ContentType): Boolean

    fun getContentTypes(direction: Direction): Set<ContentType>

    fun dependsOn(type: TypeDefinition): Boolean

    fun split(): Pair<TypeDefinition, TypeDefinition>

    fun replaceType(old: TypeDefinition, new: TypeDefinition)

}

// this is just a marker interface, so the code generator knows which type definitions can be ignored
sealed interface TypeDefinitionOverlay {

    val base: TypeDefinition

}

enum class Direction {
    // data from client to server
    Up,

    // data from server to client
    Down
}