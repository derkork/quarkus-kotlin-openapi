package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation

interface CollectionTypeDefinition : TypeDefinition {

    val items: TypeUsage

}

class RealCollectionTypeDefinition(
    override val nullable: Boolean,
    override val modifier: SchemaModifier?,
    override val items: TypeUsage,
    override val validations: List<SchemaValidation>
) : CollectionTypeDefinition {

    private val _contentTypes = mutableMapOf<Direction, MutableSet<ContentType>>()

    override val directions: Set<Direction>
        get() = _contentTypes.keys

    override fun addContentType(direction: Direction, contentType: ContentType): Boolean {
        return _contentTypes.getOrPut(direction) { mutableSetOf() }.add(contentType)
    }

    override fun getContentTypes(direction: Direction): Set<ContentType> {
        return _contentTypes[direction] ?: mutableSetOf()
    }

    override fun dependsOn(type: TypeDefinition) = items.type == type

    override fun split(): Pair<TypeDefinition, TypeDefinition> {
        val upType = RealCollectionTypeDefinition(
            nullable,
            modifier,
            TypeUsage(items.required, items.type),
            validations
        ).also {
            it._contentTypes[Direction.Up] = _contentTypes[Direction.Up] ?: mutableSetOf()
        }
        val downType = RealCollectionTypeDefinition(
            nullable,
            modifier,
            TypeUsage(items.required, items.type),
            validations
        ).also {
            it._contentTypes[Direction.Down] = _contentTypes[Direction.Down] ?: mutableSetOf()
        }
        return upType to downType
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {
        if (items.type == old) {
            items.type = new
        }
    }
}

// TODO: as the base must be mutable we can't use Delegation (not yet supported by kotlin yet). So we have to implement
//  the methods by hand.
class CollectionTypeDefinitionOverlay(
    override var base: CollectionTypeDefinition,
    private val forceNullable: Boolean,
    private val modifierOverlay: SchemaModifier?,
    private val additionalValidations: List<SchemaValidation> = listOf()
) : CollectionTypeDefinition, TypeDefinitionOverlay {

    override val nullable: Boolean
        get() = forceNullable || base.nullable

    override val modifier: SchemaModifier?
        get() = modifierOverlay ?: base.modifier

    override val items: TypeUsage
        get() = base.items

    override val validations: List<SchemaValidation>
        get() = additionalValidations + base.validations

    override val directions: Set<Direction>
        get() = base.directions

    override fun addContentType(direction: Direction, contentType: ContentType) =
        base.addContentType(direction, contentType)

    override fun getContentTypes(direction: Direction) = base.getContentTypes(direction)

    override fun dependsOn(type: TypeDefinition) = base == type

    override fun split(): Pair<TypeDefinition, TypeDefinition> {
        val upType = CollectionTypeDefinitionOverlay(base, forceNullable, modifierOverlay, additionalValidations)
        val downType = CollectionTypeDefinitionOverlay(base, forceNullable, modifierOverlay, additionalValidations)
        return upType to downType
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {
        if (base == old) {
            base = new as CollectionTypeDefinition
        }
    }
}