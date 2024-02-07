package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseDefinitionComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SomeOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.TypeComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.TypeComponent.Companion.merge
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

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

                val type = when (effectiveSchemaType) {
                    SchemaTypes.String,
                    SchemaTypes.Number,
                    SchemaTypes.Integer,
                    SchemaTypes.Boolean -> schemaDefinition.toSimpleTypeDefinition(
                        effectiveSchemaType,
                        mergedTypeComponent.format,
                        mergedTypeComponent.nullable
                    )

                    // TODO
                    SchemaTypes.Object -> PrimitiveTypeDefinition("ObjectFoo".className("narf"), false)
                    SchemaTypes.Array -> PrimitiveTypeDefinition("ArrayFoo".className("narf"), false)
                }

                schemaDefinition.typeDefinition = type
            }
        }
    }

    private inline fun <reified T> TransformableSchemaDefinition.searchInHierarchy(): List<T> {
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

    private fun TransformableSchemaDefinition.toSimpleTypeDefinition(
        effectiveType: SchemaTypes,
        format: String?,
        nullable: Boolean?
    ) = PrimitiveTypeDefinition(typeMapper.mapToPrimitiveType(effectiveType, format), nullable ?: false)
}