package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation

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
    override val validations: List<SchemaValidation>
) : ObjectTypeDefinition {

    private val _contentTypes = mutableMapOf<Direction, MutableSet<ContentType>>()

    override val directions: Set<Direction>
        get() = _contentTypes.keys

    override fun addContentType(direction: Direction, contentType: ContentType): Boolean {
        return _contentTypes.getOrPut(direction) { mutableSetOf() }.add(contentType)
    }

    override fun getContentTypes(direction: Direction): Set<ContentType> {
        return _contentTypes.getOrElse(direction) { mutableSetOf() }
    }

}

class ObjectTypeDefinitionOverlay(
    private val base: ObjectTypeDefinition,
    forceNullable: Boolean,
    additionalValidations: List<SchemaValidation> = listOf()
) : ObjectTypeDefinition by base, TypeDefinitionOverlay {

    override val nullable = forceNullable || base.nullable

    override val validations = additionalValidations + base.validations

}