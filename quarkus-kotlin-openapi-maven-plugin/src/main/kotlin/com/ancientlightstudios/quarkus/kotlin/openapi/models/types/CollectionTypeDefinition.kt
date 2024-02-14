package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.CustomConstraintsValidationComponent

// TODO: support for array validation
interface CollectionTypeDefinition : TypeDefinition {

    val modelName: ClassName

    val items: TransformableSchemaUsage

}

class RealCollectionTypeDefinition(
    override val modelName: ClassName,
    override val nullable: Boolean,
    override val items: TransformableSchemaUsage,
    override val customConstraints: List<CustomConstraintsValidationComponent>
) : CollectionTypeDefinition {

    private val _contentTypes = mutableMapOf<Direction, MutableSet<ContentType>>()

    override val directions: Set<Direction> = _contentTypes.keys

    override fun addContentType(direction: Direction, contentType: ContentType): Boolean {
        return _contentTypes.getOrPut(direction) { mutableSetOf() }.add(contentType)
    }

    override fun getContentTypes(direction: Direction): Set<ContentType> {
        return _contentTypes.getOrElse(direction) { mutableSetOf() }
    }

}

class CollectionTypeDefinitionOverlay(
    private val base: CollectionTypeDefinition,
    forceNullable: Boolean,
    additionalCustomConstraints: List<CustomConstraintsValidationComponent> = listOf()
) : CollectionTypeDefinition by base, TypeDefinitionOverlay {

    override val nullable = forceNullable || base.nullable

    override val customConstraints = additionalCustomConstraints + base.customConstraints

}