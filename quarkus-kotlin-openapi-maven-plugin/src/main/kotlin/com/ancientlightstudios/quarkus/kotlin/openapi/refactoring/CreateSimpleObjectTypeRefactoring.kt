package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// knows how to create or extend a simple object type (without any *Of stuff)
class CreateSimpleObjectTypeRefactoring(
    private val schema: TransformableSchema,
    private val lazyTypeUsage: (TypeUsage, () -> TypeDefinition) -> Unit,
    private val parentType: ObjectTypeDefinition? = null
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

    // TODO: map support
    private fun RefactoringContext.createNewType(
        type: SchemaTypes?,
        nullable: Boolean?,
        modifier: SchemaModifier?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        // anything without a type is an object
        if (type != null && type != SchemaTypes.Object) {
            ProbableBug("Incompatible type $type for an object type")
        }

        val required = schema.getComponent<ObjectValidationComponent>()?.required?.toSet() ?: setOf()
        val properties = schema.getComponent<ObjectComponent>()?.properties
            ?: ProbableBug("Object schema without properties. Found in ${schema.originPath}")

        val objectProperties = properties.map {
            val propertyTypeUsage = TypeUsage(required.contains(it.name))
            // lazy lookup in case the property schema is not yet converted
            lazyTypeUsage(propertyTypeUsage) { it.schema.typeDefinition }
            ObjectTypeProperty(it.name, it.name.variableName(), propertyTypeUsage)
        }

        return RealObjectTypeDefinition(
            schema.name.className(modelPackage),
            nullable ?: false, modifier, objectProperties, required, validations
        )
    }

    private fun RefactoringContext.createOverlayType(
        parentType: ObjectTypeDefinition,
        type: SchemaTypes?,
        nullable: Boolean?,
        modifier: SchemaModifier?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        // the type should still be the same or nothing at all
        if (type != null && type != SchemaTypes.Object) {
            ProbableBug("Incompatible type $type for an object type")
        }

        if (modifier != null && parentType.modifier != null && modifier != parentType.modifier) {
            ProbableBug("schema ${schema.originPath} has different readonly/write-only modifier than it's base schema")
        }

        val required = schema.getComponent<ObjectValidationComponent>()?.required?.toSet() ?: setOf()
        val properties = schema.getComponent<ObjectComponent>()?.properties ?: listOf()

        val requiredByBase = parentType.required
        val requiredChanged = required.subtract(requiredByBase).isNotEmpty()

        // object structure is still the same, we can just create an overlay
        if (!requiredChanged && properties.isEmpty()) {
            return ObjectTypeDefinitionOverlay(parentType, nullable == true, modifier, validations)
        }

        // something changed, we have to build a new type
        val newRequired = required + parentType.required

        // keep all properties from the base type which are not redefined here
        val filteredOldProperties =
            parentType.properties.filterNot { old -> properties.any { it.name == old.sourceName } }

        val newProperties = properties.map {
            val propertyTypeUsage = TypeUsage(newRequired.contains(it.name))
            // lazy lookup in case the property schema is not yet converted
            lazyTypeUsage(propertyTypeUsage) { it.schema.typeDefinition }
            ObjectTypeProperty(it.name, it.name.variableName(), propertyTypeUsage)
        } + filteredOldProperties

        return RealObjectTypeDefinition(
            schema.name.className(modelPackage),
            nullable == true || parentType.nullable,
            modifier ?: parentType.modifier,
            newProperties,
            newRequired,
            parentType.validations + validations
        )
    }

}