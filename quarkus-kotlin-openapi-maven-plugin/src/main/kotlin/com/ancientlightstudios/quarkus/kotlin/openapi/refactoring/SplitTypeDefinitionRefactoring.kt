package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.Direction

class SplitTypeDefinitionRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        /*
        // used to have a quick way of finding our where a type definition is coming from
        val schemaLookup = mutableMapOf<TypeDefinition, OpenApiSchema>()

        // all types which are candidates for a split
        val potentialCandidates = spec.schemas
            // primitive types don't use other types and don't need to be split
            .filterNot { it.typeDefinition is PrimitiveTypeDefinition }
            // same as primitive types
            .filterNot { it.typeDefinition is EnumTypeDefinition }
            .map {
                // fill the reverse lookup, for later use
                val typeDefinition = it.typeDefinition
                schemaLookup[typeDefinition] = it
                typeDefinition
            }

        // step 1: find all type definitions with a readonly/write-only modifier
        val typesWithModifier = spec.schemas
            .map { it.typeDefinition }
            .filter { it.modifier != null }
            .toSet()

        // step 2: find object type definitions (ignore overlays for now) with properties of one of the types from step 1
        // They must be modified and maybe even split
        val affectedObjectTypes = potentialCandidates
            .filterIsInstance<RealObjectTypeDefinition>()
            .filter { it.properties.any { property -> typesWithModifier.contains(property.typeUsage.type) } }

        // the list contains all types which were split and still needs to be replaced in the type hierarchy
        val taskList = mutableMapOf<TypeDefinition, Pair<TypeDefinition, TypeDefinition>>()

        // helper function to avoid code duplication
        val scheduleHandler: (TypeDefinition, TypeDefinition, TypeDefinition) -> Unit =
            { originalType, upType, downType ->
                // remove the now obsolete type and register the new types to the same schema
                val declaringSchema = schemaLookup.remove(originalType) ?: ProbableBug("declaring schema not found")
                declaringSchema.clearTypeDefinition()
                schemaLookup[upType] = declaringSchema
                declaringSchema.upTypeDefinition = upType
                schemaLookup[downType] = declaringSchema
                declaringSchema.downTypeDefinition = downType

                // schedule the replacement of the old type with its new types
                taskList[originalType] = upType to downType
            }

        // step 3: alter the found object types
        // a: if an object has only one direction, drop any property of the opposite direction. no further actions required
        affectedObjectTypes.updateUnidirectionalRealObject()

        // b: if an object has multiple directions, split it into two types and filter its properties. Add it to the task list
        affectedObjectTypes.splitBidirectionalRealObject(scheduleHandler)

        // from here on, we have to split types and swap type definition references, but no more structural
        // modifications like removing properties

        // step 4: iterate over the task list until it is empty, and update references
        while (taskList.isNotEmpty()) {
            taskList.pop { originalType, (upType, downType) ->
                // a copy, so we can alter the original list for the next loop, without interfering with the current loop
                val typesToCheck = schemaLookup.keys.toList()
                // replace the current bidirectional type with one of its unidirectional types in other unidirectional types
                typesToCheck.updateUnidirectionalTypes(originalType, upType, downType)
                // replace the current bidirectional type with one of its unidirectional types in other bidirectional types.
                // Add them to the task list itself
                typesToCheck.splitBidirectionalTypes(originalType, upType, downType, scheduleHandler)
            }
        }

         */
    }

    private fun filterObjectProperties(typeDefinition: RealObjectTypeDefinition, acceptedDirection: Direction) {
        // remove any property which has a modifier set and this modifier is not compatible with the given direction
        typeDefinition.properties = typeDefinition.properties.filter {
            val modifier = it.typeUsage.type.modifier
            modifier == null || when (acceptedDirection) {
                Direction.Up -> modifier == SchemaModifier.WriteOnly
                Direction.Down -> modifier == SchemaModifier.ReadOnly
            }
        }
    }

    private fun List<RealObjectTypeDefinition>.updateUnidirectionalRealObject() {
        this.filter { it.directions.size == 1 }
            .forEach { filterObjectProperties(it, it.directions.first()) }
    }

    private fun List<RealObjectTypeDefinition>.splitBidirectionalRealObject(block: (TypeDefinition, TypeDefinition, TypeDefinition) -> Unit) {
        this
            .filter { it.directions.size > 1 }
            .forEach {
                val (upType, downType) = it.split()
                filterObjectProperties(upType as RealObjectTypeDefinition, Direction.Up)
                filterObjectProperties(downType as RealObjectTypeDefinition, Direction.Down)
                block(it, upType, downType)
            }
    }

    // responsible for types with only one direction
    private fun List<TypeDefinition>.updateUnidirectionalTypes(
        originalType: TypeDefinition, upType: TypeDefinition, downType: TypeDefinition
    ) {
        // find all types (overlay or real) with only a single direction and depending on the split type. Swap the
        // original type with one of the new replacement types
        filter { it.directions.size == 1 }
            .filter { it.dependsOn(originalType) }
            .forEach {
                if (it.directions.first() == Direction.Up) {
                    it.replaceType(originalType, upType)
                } else {
                    it.replaceType(originalType, downType)
                }
            }
    }

    // responsible for types with multiple directions
    private fun List<TypeDefinition>.splitBidirectionalTypes(
        originalType: TypeDefinition, upType: TypeDefinition, downType: TypeDefinition,
        block: (TypeDefinition, TypeDefinition, TypeDefinition) -> Unit
    ) {
        // find all types (overlay or real) with multiple directions and depending on the split type. Split these types
        // too and swap the original type with one of the new replacement types
        filter { it.directions.size > 1 }
            .filter { it.dependsOn(originalType) }
            .forEach {
                val (currentUp, currentDown) = it.split()
                currentUp.replaceType(originalType, upType)
                currentDown.replaceType(originalType, downType)
                block(it, currentUp, currentDown)
            }
    }

}