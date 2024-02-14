package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.CustomConstraintsValidationComponent

// TODO: support for string and number validation
interface PrimitiveTypeDefinition : TypeDefinition {

    val baseType: ClassName

    val defaultValue: KotlinExpression?

}

class RealPrimitiveTypeDefinition(
    override val baseType: ClassName,
    override val nullable: Boolean,
    override val defaultValue: KotlinExpression?,
    override val customConstraints: List<CustomConstraintsValidationComponent>
) : PrimitiveTypeDefinition {

    private val _contentTypes = mutableMapOf<Direction, MutableSet<ContentType>>()

    override val directions: Set<Direction> = _contentTypes.keys

    override fun addContentType(direction: Direction, contentType: ContentType): Boolean {
        return _contentTypes.getOrPut(direction) { mutableSetOf() }.add(contentType)
    }

    override fun getContentTypes(direction: Direction): Set<ContentType> {
        return _contentTypes.getOrElse(direction) { mutableSetOf() }
    }

}

class PrimitiveTypeDefinitionOverlay(
    private val base: PrimitiveTypeDefinition,
    forceNullable: Boolean,
    defaultValueOverlay: KotlinExpression?,
    additionalCustomConstraints: List<CustomConstraintsValidationComponent> = listOf()
) : PrimitiveTypeDefinition by base, TypeDefinitionOverlay {

    override val nullable = forceNullable || base.nullable

    override val defaultValue = defaultValueOverlay ?: base.defaultValue

    override val customConstraints = additionalCustomConstraints + base.customConstraints

}