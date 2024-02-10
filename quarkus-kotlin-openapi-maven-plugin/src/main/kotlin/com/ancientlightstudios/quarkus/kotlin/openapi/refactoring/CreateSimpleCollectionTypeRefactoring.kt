package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ArrayItemsComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.CustomConstraintsValidationComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.NullableComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.TypeComponent
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
        val customConstraints = definition.getComponent<CustomConstraintsValidationComponent>()
            ?.let { listOf(it) } ?: listOf()

        val typeDefinition = when (parentType) {
            null -> createNewType(type, nullable, customConstraints)
            else -> createOverlayType(parentType, type, nullable, customConstraints)
        }
        definition.typeDefinition = typeDefinition
    }

    private fun RefactoringContext.createNewType(
        type: SchemaTypes?,
        nullable: Boolean?,
        customConstraints: List<CustomConstraintsValidationComponent>
    ): TypeDefinition {
        if (type != SchemaTypes.Array) {
            ProbableBug("Incompatible type $type for a collection type")
        }

        val items = definition.getComponent<ArrayItemsComponent>()?.itemsSchema
            ?: ProbableBug("Array schema without item schema. Found in ${definition.originPath}")
        return RealCollectionTypeDefinition(
            definition.name.className(modelPackage),
            nullable ?: false, items, customConstraints
        )
    }

    private fun createOverlayType(
        parentType: CollectionTypeDefinition,
        type: SchemaTypes?,
        nullable: Boolean?,
        customConstraints: List<CustomConstraintsValidationComponent>
    ): TypeDefinition {
        // the type should still be the same or nothing at all
        if (type != null && type != SchemaTypes.Array) {
            ProbableBug("Incompatible type $type for a collection type")
        }

        val items = definition.getComponent<ArrayItemsComponent>()?.itemsSchema
        if (items != null) {
            ProbableBug("Redefining the schema of array items is not allowed. Found in ${definition.originPath}")
        }

        return CollectionTypeDefinitionOverlay(parentType, nullable == true, customConstraints)
    }

}