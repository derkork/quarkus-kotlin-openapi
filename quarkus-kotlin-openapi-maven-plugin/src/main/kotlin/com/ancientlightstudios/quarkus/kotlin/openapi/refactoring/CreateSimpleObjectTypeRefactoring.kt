package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.MapComponent.Companion.mapComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.NullableComponent.Companion.nullableComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ObjectComponent.Companion.objectComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ObjectValidationComponent.Companion.objectValidationComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaModifierComponent.Companion.schemaModifierComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.SchemaValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.TypeComponent.Companion.typeComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.ValidationComponent.Companion.validationComponents
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// knows how to create or extend a simple object type (without any *Of stuff)
class CreateSimpleObjectTypeRefactoring(
    private val schema: TransformableSchema,
    private val typeResolver: TypeResolver,
    private val parentType: ObjectTypeDefinition? = null
) : SpecRefactoring {

    @Suppress("DuplicatedCode")
    override fun RefactoringContext.perform() {
        val type = schema.typeComponent()?.type
        val nullable = schema.nullableComponent()?.nullable
        val modifier = schema.schemaModifierComponent()?.modifier
        val validations = schema.validationComponents().map { it.validation }

        val typeDefinition = when (parentType) {
            null -> createNewType(type, nullable, modifier, validations)
            else -> createOverlayType(parentType, type, nullable, modifier, validations)
        }

        schema.typeDefinition = typeDefinition
    }

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

        val required = schema.objectValidationComponent()?.required?.toSet() ?: setOf()
        val properties = schema.objectComponent()?.properties ?: listOf()
        var mapValuesTypeUsage: TypeUsage? = null
        val mapValuesSchema = schema.mapComponent()?.schema?.let { schema ->
            val typeUsage = TypeUsage(true)
            typeResolver.schedule(typeUsage) { schema.typeDefinition }
            mapValuesTypeUsage = typeUsage
        }

        if (mapValuesSchema == null && properties.isEmpty()) {
            ProbableBug("Object schema without properties. Found in ${schema.originPath}")
        }

        val objectProperties = properties.map {
            val propertyTypeUsage = TypeUsage(required.contains(it.name))
            // lazy lookup in case the property schema is not yet converted
            typeResolver.schedule(propertyTypeUsage) { it.schema.typeDefinition }
            ObjectTypeProperty(it.name, it.name.variableName(), propertyTypeUsage)
        }


        return RealObjectTypeDefinition(
            schema.name.className(modelPackage),
            nullable ?: false, modifier, objectProperties, mapValuesTypeUsage, required, validations
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

        val required = schema.objectValidationComponent()?.required?.toSet() ?: setOf()
        val properties = schema.objectComponent()?.properties ?: listOf()

        val requiredByBase = parentType.required
        val requiredChanged = required.subtract(requiredByBase).isNotEmpty()

        val mapValuesSchema = schema.mapComponent()?.schema

        // object structure is still the same, we can just create an overlay
        if (!requiredChanged && properties.isEmpty() && mapValuesSchema == null) {
            return ObjectTypeDefinitionOverlay(parentType, nullable == true, modifier, validations)
        }

        // something changed, we have to build a new type
        val newRequired = required + parentType.required

        // keep all properties from the base type which are not redefined here
        val filteredOldProperties = parentType.properties
            .filterNot { old -> properties.any { it.name == old.sourceName } }
            .map {
                if (required.contains(it.sourceName) && !it.typeUsage.required) {
                    // the required information for this inherited property has changed
                    val newType = TypeUsage(true)
                    typeResolver.schedule(newType, it.typeUsage)
                    ObjectTypeProperty(it.sourceName, it.name, newType)
                } else {
                    it
                }
            }

        val newProperties = properties.map {
            val propertyTypeUsage = TypeUsage(newRequired.contains(it.name))
            // lazy lookup in case the property schema is not yet converted
            typeResolver.schedule(propertyTypeUsage) { it.schema.typeDefinition }
            ObjectTypeProperty(it.name, it.name.variableName(), propertyTypeUsage)
        } + filteredOldProperties

        var mapValuesTypeUsage: TypeUsage? = null
        mapValuesSchema?.let { schema ->
            val typeUsage = TypeUsage(true)
            typeResolver.schedule(typeUsage) { schema.typeDefinition }
            mapValuesTypeUsage = typeUsage
        }

        return RealObjectTypeDefinition(
            schema.name.className(modelPackage),
            nullable == true || parentType.nullable,
            modifier ?: parentType.modifier,
            newProperties,
            mapValuesTypeUsage ?: parentType.additionalProperties,
            newRequired,
            parentType.validations + validations
        )
    }

}