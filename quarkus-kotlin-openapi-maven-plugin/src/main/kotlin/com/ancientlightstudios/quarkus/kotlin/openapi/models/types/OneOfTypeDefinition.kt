package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation

interface OneOfTypeDefinition : TypeDefinition {

    val modelName: ClassName

    val discriminatorProperty: OneOfDiscriminatorProperty?

    val options: List<OneOfOption>

}

class RealOneOfTypeDefinition(
    override var modelName: ClassName,
    override val discriminatorProperty: OneOfDiscriminatorProperty?,
    override val nullable: Boolean,
    override val modifier: SchemaModifier?,
    override val options: List<OneOfOption>,
    override val validations: List<SchemaValidation>
) : OneOfTypeDefinition {

    private val _contentTypes = mutableMapOf<Direction, MutableSet<ContentType>>()

    override val directions: Set<Direction>
        get() = _contentTypes.keys

    override fun addContentType(direction: Direction, contentType: ContentType): Boolean {
        return _contentTypes.getOrPut(direction) { mutableSetOf() }.add(contentType)
    }

    override fun getContentTypes(direction: Direction): Set<ContentType> {
        return _contentTypes[direction] ?: mutableSetOf()
    }

    override fun dependsOn(type: TypeDefinition) = options.any { it.typeUsage.type == type }

    override fun split(): Pair<TypeDefinition, TypeDefinition> {
        val upType = RealOneOfTypeDefinition(
            modelName.extend(postfix = "Up"),
            discriminatorProperty,
            nullable,
            modifier,
            options.map {
                OneOfOption(it.modelName, TypeUsage(it.typeUsage.required, it.typeUsage.type), it.aliases)
            },
            validations
        ).also {
            it._contentTypes[Direction.Up] = _contentTypes[Direction.Up] ?: mutableSetOf()
        }
        val downType = RealOneOfTypeDefinition(
            modelName.extend(postfix = "Down"),
            discriminatorProperty,
            nullable,
            modifier,
            options.map {
                OneOfOption(it.modelName, TypeUsage(it.typeUsage.required, it.typeUsage.type), it.aliases)
            },
            validations
        ).also {
            it._contentTypes[Direction.Down] = _contentTypes[Direction.Down] ?: mutableSetOf()
        }
        return upType to downType
    }

    override fun replaceType(old: TypeDefinition, new: TypeDefinition) {
        options.filter { it.typeUsage.type == old }.forEach { it.typeUsage.type = new }
    }
}
