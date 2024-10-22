package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ReferencingComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.TypeComponent.Companion.typeComponent

// converts the most basic form of a schema into a type. Is responsible for schemas without any base ref or *Of component.
// Because these schemas don't have other dependencies they will always receive a real type and never an overlay
class AssignTypesToSimpleSchemasRefactoring(
    private val tasks: MutableSet<TransformableSchema>,
    private val typeMapper: TypeMapper,
    private val typeResolver: TypeResolver
) :
    SpecRefactoring {

    override fun RefactoringContext.perform() {
        // find all schemas, without referencing components (base definition, *Of)
        val candidates = tasks.filter { definition -> definition.components.none { it is ReferencingComponent } }
            .toSet()

        // remove them from the tasks list, because nobody has to handle them anymore
        tasks.removeAll(candidates)

        // now transform each of them
        candidates.forEach { definition ->
            val type = definition.typeComponent()?.type ?: SchemaTypes.Object

            when (type) {
                SchemaTypes.String,
                SchemaTypes.Number,
                SchemaTypes.Integer,
                SchemaTypes.Boolean -> performRefactoring(CreateSimplePrimitiveTypeRefactoring(typeMapper, definition))

                SchemaTypes.Object -> performRefactoring(CreateSimpleObjectTypeRefactoring(definition, typeResolver))
                SchemaTypes.Array -> performRefactoring(
                    CreateSimpleCollectionTypeRefactoring(definition, typeResolver)
                )
            }
        }
    }

}