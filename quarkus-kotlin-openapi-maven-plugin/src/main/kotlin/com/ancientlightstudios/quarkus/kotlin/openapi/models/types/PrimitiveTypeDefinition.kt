package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

interface PrimitiveTypeDefinition : TypeDefinition {

    val baseType: ClassName

    val defaultValue: KotlinExpression?

}

class RealPrimitiveTypeDefinition(
    override val baseType: ClassName,
    override val nullable: Boolean,
    override val modifier: SchemaModifier?,
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
        return _contentTypes[direction] ?: mutableSetOf()
    }

    override fun dependsOn(type: TypeDefinition) = false

    override fun split(): Pair<TypeDefinition, TypeDefinition> {
        ProbableBug("split for primitive types not defined")
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {}

}

// TODO: as the base must be mutable we can't use Delegation (not yet supported by kotlin yet). So we have to implement
//  the methods by hand.
class PrimitiveTypeDefinitionOverlay(
    override var base: PrimitiveTypeDefinition,
    private val forceNullable: Boolean,
    private val modifierOverlay: SchemaModifier?,
    private val defaultValueOverlay: KotlinExpression?,
    private val additionalValidations: List<SchemaValidation> = listOf()
) : PrimitiveTypeDefinition, TypeDefinitionOverlay {

    override val baseType: ClassName
        get() = base.baseType

    override val nullable: Boolean
        get() = forceNullable || base.nullable

    override val modifier: SchemaModifier?
        get() = modifierOverlay ?: base.modifier

    override val defaultValue: KotlinExpression?
        get() = defaultValueOverlay ?: base.defaultValue

    override val validations: List<SchemaValidation>
        get() = additionalValidations + base.validations

    override val directions: Set<Direction>
        get() = base.directions

    override fun addContentType(direction: Direction, contentType: ContentType) =
        base.addContentType(direction, contentType)

    override fun getContentTypes(direction: Direction) = base.getContentTypes(direction)

    override fun dependsOn(type: TypeDefinition) = false

    override fun split(): Pair<TypeDefinition, TypeDefinition> {
        ProbableBug("split for primitive types not defined")
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {}
}