package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition

// specifies which content types are used in which direction
object ContentTypesHint : Hint<AssignedContentTypes> {

    fun TransformableSchemaDefinition.contentTypes(direction: Direction): Set<ContentType> {
        val data = get(ContentTypesHint) ?: return emptySet()

        return when (direction) {
            Direction.Up -> data.up
            Direction.Down -> data.down
        }
    }

    fun TransformableSchemaDefinition.addContentType(direction: Direction, vararg contentType: ContentType): Boolean {
        if (contentType.isEmpty()) {
            return false
        }

        val data = getOrPut(ContentTypesHint) { AssignedContentTypes(mutableSetOf(), mutableSetOf()) }
        return when (direction) {
            Direction.Up -> data.up.addAll(contentType)
            Direction.Down -> data.down.addAll(contentType)
        }
    }

}

class AssignedContentTypes(val up: MutableSet<ContentType>, val down: MutableSet<ContentType>)