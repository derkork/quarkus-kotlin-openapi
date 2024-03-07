package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinitionOverlay
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.RealCollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// knows how to create or extend a simple collection type (without any *Of stuff)
class CreateSimpleCollectionTypeRefactoring(
    private val definition: TransformableSchemaDefinition,
    private val parentType: CollectionTypeDefinition? = null
) : SpecRefactoring {

    @Suppress("DuplicatedCode")
    override fun RefactoringContext.perform() {
        val type = definition.getComponent<TypeComponent>()?.type
        val nullable = definition.getComponent<NullableComponent>()?.nullable
        val validations = definition.getComponents<ValidationComponent>().map { it.validation }

        val typeDefinition = when (parentType) {
            null -> createNewType(type, nullable, validations)
            else -> createOverlayType(parentType, type, nullable, validations)
        }
        definition.typeDefinition = typeDefinition
    }

    private fun RefactoringContext.createNewType(
        type: SchemaTypes?,
        nullable: Boolean?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        if (type != SchemaTypes.Array) {
            ProbableBug("Incompatible type $type for a collection type")
        }

        val items = definition.getComponent<ArrayItemsComponent>()?.itemsSchema
            ?: ProbableBug("Array schema without item schema. Found in ${definition.originPath}")
        return RealCollectionTypeDefinition(
            definition.name.className(modelPackage),
            nullable ?: false, items, validations
        )
    }

    private fun createOverlayType(
        parentType: CollectionTypeDefinition,
        type: SchemaTypes?,
        nullable: Boolean?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        // the type should still be the same or nothing at all
        if (type != null && type != SchemaTypes.Array) {
            ProbableBug("Incompatible type $type for a collection type")
        }

        if (definition.getComponent<ArrayItemsComponent>()?.itemsSchema != null) {
            ProbableBug("Redefining the schema of array items is not allowed. Found in ${definition.originPath}")
        }

        return CollectionTypeDefinitionOverlay(parentType, nullable == true, validations)
    }

}