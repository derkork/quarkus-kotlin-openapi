package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// knows how to create or extend a simple object type (without any *Of stuff)
class CreateSimpleObjectTypeRefactoring(
    private val definition: TransformableSchemaDefinition,
    private val parentType: ObjectTypeDefinition? = null
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

    // TODO: map support
    private fun RefactoringContext.createNewType(
        type: SchemaTypes?,
        nullable: Boolean?,
        customConstraints: List<CustomConstraintsValidationComponent>
    ): TypeDefinition {
        // anything without a type is an object
        if (type != null && type != SchemaTypes.Object) {
            ProbableBug("Incompatible type $type for an object type")
        }

        val required = definition.getComponent<ObjectValidationComponent>()?.required?.toSet() ?: setOf()
        val properties = definition.getComponent<ObjectComponent>()?.properties
            ?: ProbableBug("Object schema without properties. Found in ${definition.originPath}")

        val objectProperties = properties.map {
            ObjectTypeProperty(it.name, it.name.variableName(), it.schema)
        }

        return RealObjectTypeDefinition(
            definition.name.className(modelPackage),
            nullable ?: false, objectProperties, required, customConstraints
        )
    }

    private fun RefactoringContext.createOverlayType(
        parentType: ObjectTypeDefinition,
        type: SchemaTypes?,
        nullable: Boolean?,
        customConstraints: List<CustomConstraintsValidationComponent>
    ): TypeDefinition {
        // the type should still be the same or nothing at all
        if (type != null && type != SchemaTypes.Object) {
            ProbableBug("Incompatible type $type for an object type")
        }

        val required = definition.getComponent<ObjectValidationComponent>()?.required?.toSet() ?: setOf()
        val properties = definition.getComponent<ObjectComponent>()?.properties ?: listOf()

        val requiredByBase = parentType.required
        val requiredChanged = required.subtract(requiredByBase).isNotEmpty()

        // object structure is still the same, we can just create a overlay
        if (!requiredChanged && properties.isEmpty()) {
            return ObjectTypeDefinitionOverlay(parentType, nullable == true, customConstraints)
        }

        // something changed, we have to build a new type
        val newRequired = required + parentType.required

        // keep all properties from the base type which are not redefined here
        val filteredOldProperties =
            parentType.properties.filterNot { old -> properties.any { it.name == old.sourceName } }

        val newProperties = properties.map {
            ObjectTypeProperty(it.name, it.name.variableName(), it.schema)
        } + filteredOldProperties

        return RealObjectTypeDefinition(
            definition.name.className(modelPackage),
            nullable == true || parentType.nullable,
            newProperties,
            newRequired,
            parentType.customConstraints + customConstraints
        )
    }

}