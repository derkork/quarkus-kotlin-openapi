package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.hasTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.BaseSchemaComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SomeOfComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

// converts schema with a base ref but no *Of component into a type. This can lead to new real types or just
// overlays.
class AssignTypesToSimpleExtendedSchemasRefactoring(
    private val tasks: MutableSet<TransformableSchema>,
    private val typeMapper: TypeMapper,
    private val lazyTypeUsage: (TypeUsage, () -> TypeDefinition) -> Unit
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // find all schemas, with a base ref but no *Of component
        // we know this cast is valid, because of the filter
        @Suppress("UNCHECKED_CAST")
        val candidates = tasks
            .filter { schema -> schema.components.none { it is SomeOfComponent } }
            .map { it to it.getComponent<BaseSchemaComponent>() }
            .filter { it.second != null }
            .filter { it.second!!.schema.hasTypeDefinition }
                as List<Pair<TransformableSchema, BaseSchemaComponent>>

        // remove them from the tasks list, because nobody has to handle them anymore
        tasks.removeAll(candidates.map { it.first }.toSet())

        // now transform each of them
        candidates.forEach { (schema, baseComponent) ->
            when (val baseType = baseComponent.schema.typeDefinition) {
                is PrimitiveTypeDefinition,
                is EnumTypeDefinition -> performRefactoring(
                    CreateSimplePrimitiveTypeRefactoring(typeMapper, schema, baseType)
                )

                is ObjectTypeDefinition -> performRefactoring(
                    CreateSimpleObjectTypeRefactoring(schema, lazyTypeUsage, baseType)
                )

                is CollectionTypeDefinition -> performRefactoring(
                    CreateSimpleCollectionTypeRefactoring(schema, lazyTypeUsage, baseType)
                )
            }
        }
    }

}