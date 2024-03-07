package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation

interface PrimitiveTypeDefinition : TypeDefinition {

    val baseType: ClassName

    val defaultValue: KotlinExpression?

}

class RealPrimitiveTypeDefinition(
    override val baseType: ClassName,
    override val nullable: Boolean,
    override val defaultValue: KotlinExpression?,
    override val validations: List<SchemaValidation>
) : PrimitiveTypeDefinition {

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

class PrimitiveTypeDefinitionOverlay(
    private val base: PrimitiveTypeDefinition,
    forceNullable: Boolean,
    defaultValueOverlay: KotlinExpression?,
    additionalValidations: List<SchemaValidation> = listOf()
) : PrimitiveTypeDefinition by base, TypeDefinitionOverlay {

    override val nullable = forceNullable || base.nullable

    override val defaultValue = defaultValueOverlay ?: base.defaultValue

    override val validations = additionalValidations + base.validations

}