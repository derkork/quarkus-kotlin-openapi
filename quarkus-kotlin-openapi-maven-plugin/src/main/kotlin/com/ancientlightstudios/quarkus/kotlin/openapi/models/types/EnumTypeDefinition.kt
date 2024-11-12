package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.companionObject
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

interface EnumTypeDefinition : TypeDefinition {

    val modelName: ClassName

    val baseType: ClassName

    val items: List<EnumTypeItem>

    val defaultValue: String?

    fun defaultExpression(): KotlinExpression? = defaultValue?.let { value ->
        modelName.companionObject().property(items.first { it.sourceName == value }.name)
    }

}

class RealEnumTypeDefinition(
    override var modelName: ClassName,
    override val baseType: ClassName,
    override val nullable: Boolean,
    override val modifier: SchemaModifier?,
    override val items: List<EnumTypeItem>,
    override val defaultValue: String?,
    override val validations: List<SchemaValidation>
) : EnumTypeDefinition {

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
        ProbableBug("split for enum types not defined")
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {}

}

// TODO: as the base must be mutable we can't use Delegation (not yet supported by kotlin yet). So we have to implement
//  the methods by hand.
class EnumTypeDefinitionOverlay(
    override var base: EnumTypeDefinition,
    private val forceNullable: Boolean,
    private val modifierOverlay: SchemaModifier?,
    private val defaultValueOverlay: String?,
    private val additionalValidations: List<SchemaValidation> = listOf()
) : EnumTypeDefinition, TypeDefinitionOverlay {

    override val modelName: ClassName
        get() = base.modelName

    override val baseType: ClassName
        get() = base.baseType

    override val nullable: Boolean
        get() = forceNullable || base.nullable

    override val modifier: SchemaModifier?
        get() = modifierOverlay ?: base.modifier

    override val items: List<EnumTypeItem>
        get() = base.items

    override val defaultValue: String?
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
        ProbableBug("split for enum types not defined")
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {}
}