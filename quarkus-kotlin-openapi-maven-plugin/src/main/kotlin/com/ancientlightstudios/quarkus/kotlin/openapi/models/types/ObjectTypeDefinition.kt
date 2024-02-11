package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.CustomConstraintsValidationComponent

// TODO: add support for maps
interface ObjectTypeDefinition : TypeDefinition {

    val modelName: ClassName

    val properties: List<ObjectTypeProperty>

    val required: Set<String>

}

class RealObjectTypeDefinition(
    override val modelName: ClassName,
    override val nullable: Boolean,
    override val properties: List<ObjectTypeProperty>,
    override val required: Set<String>,
    override val customConstraints: List<CustomConstraintsValidationComponent>
) : ObjectTypeDefinition {

    private val _contentTypes = mutableSetOf<ContentType>()
    private val _directions = mutableSetOf<Direction>()

    override val contentTypes: Set<ContentType> = _contentTypes
    override val directions: Set<Direction> = _directions

    override fun addContentType(contentType: ContentType) = _contentTypes.add(contentType)

    override fun addDirection(direction: Direction) = _directions.add(direction)

}

class ObjectTypeDefinitionOverlay(
    private val base: ObjectTypeDefinition,
    forceNullable: Boolean,
    additionalCustomConstraints: List<CustomConstraintsValidationComponent> = listOf()
) : ObjectTypeDefinition by base, TypeDefinitionOverlay {

    override val nullable = forceNullable || base.nullable

    override val customConstraints = additionalCustomConstraints + base.customConstraints

}