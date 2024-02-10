package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.hasTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.CollectionTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.PrimitiveTypeDefinition

// converts schema with a base ref but no *Of component into a type. This can lead to new real types or just
// overlays.
class AssignTypesToSimpleExtendedDefinitionsRefactoring(
    private val tasks: MutableSet<TransformableSchemaDefinition>,
    private val typeMapper: TypeMapper
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // find all schemas, with a base ref but no *Of component
        // we know this cast is valid, because of the filter
        @Suppress("UNCHECKED_CAST")
        val candidates = tasks
            .filter { definition -> definition.components.none { it is SomeOfComponent } }
            .map { it to it.getComponent<BaseDefinitionComponent>() }
            .filter { it.second != null }
            .filterNot { it.second!!.innerSchema.hasTypeDefinition }
                as List<Pair<TransformableSchemaDefinition, BaseDefinitionComponent>>

        // remove them from the tasks list, because nobody has to handle them anymore
        tasks.removeAll(candidates.map { it.first }.toSet())

        // now transform each of them
        candidates.forEach { (definition, baseComponent) ->
            when (val baseType = baseComponent.innerSchema.typeDefinition) {
                is PrimitiveTypeDefinition,
                is EnumTypeDefinition -> performRefactoring(
                    CreateSimplePrimitiveTypeRefactoring(typeMapper, definition, baseType)
                )

                is ObjectTypeDefinition -> performRefactoring(CreateSimpleObjectTypeRefactoring(definition, baseType))
                is CollectionTypeDefinition -> performRefactoring(
                    CreateSimpleCollectionTypeRefactoring(definition, baseType)
                )
            }
        }
    }

}
