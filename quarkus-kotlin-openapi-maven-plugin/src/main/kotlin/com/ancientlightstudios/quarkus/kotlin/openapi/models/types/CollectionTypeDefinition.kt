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

    private val _contentTypes = mutableSetOf<ContentType>()
    private val _directions = mutableSetOf<Direction>()

    override val contentTypes: Set<ContentType> = _contentTypes
    override val directions: Set<Direction> = _directions

    override fun addContentType(contentType: ContentType) = _contentTypes.add(contentType)

    override fun addDirection(direction: Direction) = _directions.add(direction)

}

class CollectionTypeDefinitionOverlay(
    private val base: CollectionTypeDefinition,
    forceNullable: Boolean,
    additionalCustomConstraints: List<CustomConstraintsValidationComponent> = listOf()
) : CollectionTypeDefinition by base, TypeDefinitionOverlay {

    override val nullable = forceNullable || base.nullable

    override val customConstraints = additionalCustomConstraints + base.customConstraints

}