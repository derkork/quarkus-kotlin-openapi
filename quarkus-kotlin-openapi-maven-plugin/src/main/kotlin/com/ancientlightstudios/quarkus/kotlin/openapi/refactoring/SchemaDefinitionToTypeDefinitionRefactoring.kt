package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaProperty
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaDefinitionComponent.Companion.singleOrNone
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.TypeComponent.Companion.merge
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

class SchemaDefinitionToTypeDefinitionRefactoring(private val typeMapper: TypeMapper) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        spec.inspect {
            schemaDefinitions {
                val mergedTypeComponent = schemaDefinition.searchInHierarchy<TypeComponent>().merge()
                    ?: TypeComponent()

                val effectiveSchemaType = when (mergedTypeComponent.types.size) {
                    0 -> SchemaTypes.Object
                    1 -> mergedTypeComponent.types.first()
                    else -> ProbableBug("Unambiguous type for schema definition ${schemaDefinition.originPath}")
                }

                val nullable = mergedTypeComponent.nullable ?: false

                val type = when (effectiveSchemaType) {
                    SchemaTypes.String,
                    SchemaTypes.Number,
                    SchemaTypes.Integer,
                    SchemaTypes.Boolean -> toSimpleTypeDefinition(
                        schemaDefinition,
                        effectiveSchemaType,
                        mergedTypeComponent.format,
                        nullable
                    )

                    SchemaTypes.Object -> toObjectTypeDefinition(schemaDefinition, nullable)
                    SchemaTypes.Array -> toCollectionTypeDefinition(schemaDefinition, nullable)
                }

                schemaDefinition.typeDefinition = type
            }
        }
    }

    private inline fun <reified T : SchemaDefinitionComponent> TransformableSchemaDefinition.searchInHierarchy(): List<T> {
        val result = mutableListOf<T>()

        var next: TransformableSchemaDefinition? = this
        while (next != null) {
            // take components added directly to the current schema definition
            next.components.filterIsInstanceTo(result)

            // check if we have a base definition ($ref) or a *of component with exactly one entry
            next = next.components.filterIsInstance<BaseDefinitionComponent>().firstOrNull()
                ?.innerSchema?.schemaDefinition
                ?: components.filterIsInstance<SomeOfComponent>().firstOrNull { it.schemas.size == 1 }
                    ?.schemas?.first()?.schemaDefinition
        }
        return result
    }

    private fun RefactoringContext.toSimpleTypeDefinition(
        definition: TransformableSchemaDefinition,
        effectiveType: SchemaTypes,
        format: String?,
        nullable: Boolean
    ): TypeDefinition {
        val baseType = typeMapper.mapToPrimitiveType(effectiveType, format)
        return when (val enumComponent = definition.searchInHierarchy<EnumValidationComponent>().singleOrNone()) {
            null -> PrimitiveTypeDefinition(baseType, nullable)
            else -> EnumTypeDefinition(
                definition.name.className(modelPackage),
                nullable,
                baseType,
                enumComponent.values
            )
        }
    }

    private fun RefactoringContext.toObjectTypeDefinition(
        definition: TransformableSchemaDefinition,
        nullable: Boolean
    ): TypeDefinition {
        val allOfComponent = definition.searchInHierarchy<AllOfComponent>().singleOrNone()
        if (allOfComponent != null) {
            return toAllOfObjectTypeDefinition(definition, allOfComponent, nullable)
        }

        val anyOfComponent = definition.searchInHierarchy<AnyOfComponent>().singleOrNone()
        if (anyOfComponent != null) {
            return toAnyOfObjectTypeDefinition(definition, anyOfComponent, nullable)
        }

        val oneOfComponent = definition.searchInHierarchy<OneOfComponent>().singleOrNone()
        if (oneOfComponent != null) {
            return toOneOfObjectTypeDefinition(definition, oneOfComponent, nullable)
        }

        return toDefaultObjectTypeDefinition(definition, nullable)
    }

    // TODO: this doesn't cover deeply nested objects and all other kind of allOf combinations
    private fun RefactoringContext.toAllOfObjectTypeDefinition(
        definition: TransformableSchemaDefinition,
        allOfComponent: AllOfComponent,
        nullable: Boolean
    ): TypeDefinition {
        val properties = mutableSetOf<TransformableSchemaProperty>()
        properties.addAll(definition.searchInHierarchy<ObjectComponent>().flatMap { it.properties })

        allOfComponent.schemas.forEach {
            properties.addAll(it.schemaDefinition.searchInHierarchy<ObjectComponent>().flatMap { it.properties })
        }

        return ObjectTypeDefinition(definition.name.className(modelPackage), nullable, properties.toList())
    }

    private fun RefactoringContext.toAnyOfObjectTypeDefinition(
        definition: TransformableSchemaDefinition,
        anyOfComponent: AnyOfComponent,
        nullable: Boolean
    ): TypeDefinition {
        return AnyOfTypeDefinition(definition.name.className(modelPackage), nullable, anyOfComponent.schemas)
    }

    private fun RefactoringContext.toOneOfObjectTypeDefinition(
        definition: TransformableSchemaDefinition,
        oneOfComponent: OneOfComponent,
        nullable: Boolean
    ): TypeDefinition {
        return OneOfTypeDefinition(definition.name.className(modelPackage), nullable, oneOfComponent.schemas)
    }

    private fun RefactoringContext.toDefaultObjectTypeDefinition(
        definition: TransformableSchemaDefinition,
        nullable: Boolean
    ): TypeDefinition {
        val properties = mutableSetOf<TransformableSchemaProperty>()
        properties.addAll(definition.searchInHierarchy<ObjectComponent>().flatMap { it.properties })
        return ObjectTypeDefinition(definition.name.className(modelPackage), nullable, properties.toList())
    }

    private fun RefactoringContext.toCollectionTypeDefinition(
        definition: TransformableSchemaDefinition,
        nullable: Boolean
    ): TypeDefinition {

        val itemsComponent = definition.searchInHierarchy<ArrayItemsComponent>().singleOrNone()
            ?: SpecIssue("array type without items schema is not supported")

        return CollectionTypeDefinition(itemsComponent.itemsSchema, nullable)
    }
}