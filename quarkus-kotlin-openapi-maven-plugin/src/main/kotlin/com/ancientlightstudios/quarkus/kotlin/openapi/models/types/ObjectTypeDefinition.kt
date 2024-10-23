package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation

interface ObjectTypeDefinition : TypeDefinition {

    val modelName: ClassName

    val properties: List<ObjectTypeProperty>

    val additionalProperties: TypeUsage?

    val isPureMap: Boolean

    val required: Set<String>

}

class RealObjectTypeDefinition(
    override var modelName: ClassName,
    override val nullable: Boolean,
    override val modifier: SchemaModifier?,
    override var properties: List<ObjectTypeProperty>,
    override val additionalProperties: TypeUsage?,
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
        return _contentTypes[direction] ?: mutableSetOf()
    }

    override fun dependsOn(type: TypeDefinition) = properties.any { it.typeUsage.type == type }

    override fun split(): Pair<TypeDefinition, TypeDefinition> {
        val upType = RealObjectTypeDefinition(
            modelName.extend(postfix = "Up"),
            nullable,
            modifier,
            properties.map {
                ObjectTypeProperty(it.sourceName, it.name, TypeUsage(it.typeUsage.required, it.typeUsage.type))
            },
            additionalProperties?.let { TypeUsage(it.required, it.type) },
            required,
            validations
        ).also {
            it._contentTypes[Direction.Up] = _contentTypes[Direction.Up] ?: mutableSetOf()
        }
        val downType = RealObjectTypeDefinition(
            modelName.extend(postfix = "Down"),
            nullable,
            modifier,
            properties.map {
                ObjectTypeProperty(it.sourceName, it.name, TypeUsage(it.typeUsage.required, it.typeUsage.type))
            },
            additionalProperties?.let { TypeUsage(it.required, it.type) },
            required,
            validations
        ).also {
            it._contentTypes[Direction.Down] = _contentTypes[Direction.Down] ?: mutableSetOf()
        }
        return upType to downType
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {
        properties.filter { it.typeUsage.type == old }
            .forEach { it.typeUsage.type = new }
        
        if (additionalProperties?.type == old) {
            additionalProperties.type = new
        }
    }

    override val isPureMap: Boolean
        get() = additionalProperties != null && properties.isEmpty()
}

// TODO: as the base must be mutable we can't use Delegation (not yet supported by kotlin yet). So we have to implement
//  the methods by hand.
class ObjectTypeDefinitionOverlay(
    override var base: ObjectTypeDefinition,
    private val forceNullable: Boolean,
    private val modifierOverlay: SchemaModifier?,
    private val additionalValidations: List<SchemaValidation> = listOf()
) : ObjectTypeDefinition, TypeDefinitionOverlay {

    override val modelName: ClassName
        get() = base.modelName

    override val nullable: Boolean
        get() = forceNullable || base.nullable

    override val modifier: SchemaModifier?
        get() = modifierOverlay ?: base.modifier

    override val properties: List<ObjectTypeProperty>
        get() = base.properties

    override val additionalProperties: TypeUsage?
        get() = base.additionalProperties

    override val isPureMap: Boolean
        get() = base.isPureMap

    override val required: Set<String>
        get() = base.required

    override val validations: List<SchemaValidation>
        get() = additionalValidations + base.validations

    override val directions: Set<Direction>
        get() = base.directions

    override fun addContentType(direction: Direction, contentType: ContentType) =
        base.addContentType(direction, contentType)

    override fun getContentTypes(direction: Direction) = base.getContentTypes(direction)

    override fun dependsOn(type: TypeDefinition) = base == type

    override fun split(): Pair<TypeDefinition, TypeDefinition> {
        val upType = ObjectTypeDefinitionOverlay(base, forceNullable, modifierOverlay, additionalValidations)
        val downType = ObjectTypeDefinitionOverlay(base, forceNullable, modifierOverlay, additionalValidations)
        return upType to downType
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {
        if (base == old) {
            base = new as ObjectTypeDefinition
        }
    }
}