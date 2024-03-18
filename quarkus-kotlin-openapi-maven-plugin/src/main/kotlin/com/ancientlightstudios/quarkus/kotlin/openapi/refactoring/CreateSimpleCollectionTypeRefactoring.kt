package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// knows how to create or extend a simple collection type (without any *Of stuff)
class CreateSimpleCollectionTypeRefactoring(
    private val schema: TransformableSchema,
    private val lazyTypeUsage: (TypeUsage, () -> TypeDefinition) -> Unit,
    private val parentType: CollectionTypeDefinition? = null
) : SpecRefactoring {

    @Suppress("DuplicatedCode")
    override fun RefactoringContext.perform() {
        val type = schema.getComponent<TypeComponent>()?.type
        val nullable = schema.getComponent<NullableComponent>()?.nullable
        val modifier = schema.getComponent<SchemaModifierComponent>()?.modifier
        val validations = schema.getComponents<ValidationComponent>().map { it.validation }

        val typeDefinition = when (parentType) {
            null -> createNewType(type, nullable, modifier, validations)
            else -> createOverlayType(parentType, type, nullable, modifier, validations)
        }

        schema.typeDefinition = typeDefinition
    }

    private fun RefactoringContext.createNewType(
        type: SchemaTypes?,
        nullable: Boolean?,
        modifier: SchemaModifier?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        if (type != SchemaTypes.Array) {
            ProbableBug("Incompatible type $type for a collection type")
        }

        val items = schema.getComponent<ArrayItemsComponent>()?.schema
            ?: ProbableBug("Array schema without item schema. Found in ${schema.originPath}")

        // by default array items are always required. But it's still possible to define the schema as nullable
        val itemTypeUsage = TypeUsage(true)
        // lazy lookup in case the item schema is not yet converted
        lazyTypeUsage(itemTypeUsage) { items.typeDefinition }

        return RealCollectionTypeDefinition(
            schema.name.className(modelPackage),
            nullable ?: false, modifier, itemTypeUsage, validations
        )
    }

    private fun createOverlayType(
        parentType: CollectionTypeDefinition,
        type: SchemaTypes?,
        nullable: Boolean?,
        modifier: SchemaModifier?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        // the type should still be the same or nothing at all
        if (type != null && type != SchemaTypes.Array) {
            ProbableBug("Incompatible type $type for a collection type")
        }

        if (schema.getComponent<ArrayItemsComponent>()?.schema != null) {
            ProbableBug("Redefining the schema of array items is not allowed. Found in ${schema.originPath}")
        }

        if (modifier != null && parentType.modifier != null && modifier != parentType.modifier) {
            ProbableBug("schema ${schema.originPath} has different readonly/write-only modifier than it's base schema")
        }

        return CollectionTypeDefinitionOverlay(parentType, nullable == true, modifier, validations)
    }

}