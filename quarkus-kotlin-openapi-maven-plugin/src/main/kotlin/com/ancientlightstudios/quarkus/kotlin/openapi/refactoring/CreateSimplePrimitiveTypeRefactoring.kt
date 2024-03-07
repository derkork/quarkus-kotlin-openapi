package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ConstantName.Companion.constantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.literalFor
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// knows how to create or extend a simple primitive type (without any *Of stuff)
class CreateSimplePrimitiveTypeRefactoring(
    private val typeMapper: TypeMapper,
    private val definition: TransformableSchemaDefinition,
    private val parentType: TypeDefinition? = null,
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
        val type = definition.getComponent<TypeComponent>()?.type
        val nullable = definition.getComponent<NullableComponent>()?.nullable
        val validations = definition.getComponents<ValidationComponent>().map { it.validation }

        val typeDefinition = when (parentType) {
            null -> createNewType(type, nullable, validations)
            is PrimitiveTypeDefinition -> createOverlayTypeForSimplePrimitive(
                parentType,
                type,
                nullable,
                validations
            )

            is EnumTypeDefinition -> createOverlayTypeForEnum(parentType, type, nullable, validations)
            else -> ProbableBug("incompatible base type for a primitive overlay")
        }
        definition.typeDefinition = typeDefinition
    }

    private fun RefactoringContext.createNewType(
        type: SchemaTypes?,
        nullable: Boolean?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        return when (type) {
            SchemaTypes.String,
            SchemaTypes.Number,
            SchemaTypes.Integer,
            SchemaTypes.Boolean -> generatePrimitiveType(type, nullable ?: false, validations)

            else -> ProbableBug("Incompatible type $type for a primitive type")
        }
    }

    private fun RefactoringContext.generatePrimitiveType(
        type: SchemaTypes,
        nullable: Boolean,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        val format = definition.getComponent<FormatComponent>()?.format

        val baseType = typeMapper.mapToPrimitiveType(type, format)
        val default = definition.getComponent<DefaultComponent>()?.let { baseType.literalFor(it.default) }

        return when (val enumComponent = definition.getComponent<EnumValidationComponent>()) {
            null -> RealPrimitiveTypeDefinition(baseType, nullable, default, validations)
            else -> generateEnumType(baseType, nullable, default, enumComponent, validations)
        }
    }

    private fun RefactoringContext.generateEnumType(
        baseType: ClassName,
        nullable: Boolean,
        default: KotlinExpression?,
        enumComponent: EnumValidationComponent,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        val items = enumComponent.values.map { EnumTypeItem(it, it.constantName(), baseType.literalFor(it)) }
        return RealEnumTypeDefinition(
            definition.name.className(modelPackage),
            baseType, nullable, items, default, validations
        )
    }

    private fun RefactoringContext.createOverlayTypeForSimplePrimitive(
        parentType: PrimitiveTypeDefinition,
        type: SchemaTypes?,
        nullable: Boolean?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        // TODO: if type and/or format are redefined, the type mapper must return the same class name,
        //  otherwise it's not compatible and we should fail

        // check if we have to override the default value
        val default = definition.getComponent<DefaultComponent>()?.let { parentType.baseType.literalFor(it.default) }
        val enumItems = definition.getComponent<EnumValidationComponent>()?.values ?: listOf()

        if (enumItems.isEmpty()) {
            // just an overlay
            return PrimitiveTypeDefinitionOverlay(parentType, nullable == true, default, validations)
        }

        // we have to build an enum type out of it
        val items = enumItems.map {
            EnumTypeItem(it, it.constantName(), parentType.baseType.literalFor(it))
        }

        return RealEnumTypeDefinition(
            definition.name.className(modelPackage),
            parentType.baseType,
            nullable == true || parentType.nullable,
            items,
            default ?: parentType.defaultValue,
            parentType.validations + validations
        )
    }

    private fun RefactoringContext.createOverlayTypeForEnum(
        parentType: EnumTypeDefinition,
        type: SchemaTypes?,
        nullable: Boolean?,
        validations: List<SchemaValidation>
    ): TypeDefinition {
        // TODO: if type and/or format are redefined, the type mapper must return the same class name,
        //  otherwise it's not compatible and we should fail

        // check if we have to override the default value
        val default = definition.getComponent<DefaultComponent>()?.let { parentType.baseType.literalFor(it.default) }

        val enumItems = definition.getComponent<EnumValidationComponent>()?.values ?: listOf()
        val enumChanged = enumItems.toSet().subtract(parentType.items.toSet()).isNotEmpty()

        // enum structure is still the same, we can just create an overlay
        if (!enumChanged) {
            return EnumTypeDefinitionOverlay(parentType, nullable == true, default, validations)
        }

        val newItems = enumItems.toSet().union(parentType.items.map { it.sourceName }).map {
            EnumTypeItem(it, it.constantName(), parentType.baseType.literalFor(it))
        }

        // new items available, we have to create a new enum
        return RealEnumTypeDefinition(
            definition.name.className(modelPackage),
            parentType.baseType,
            nullable == true || parentType.nullable,
            newItems,
            default ?: parentType.defaultValue,
            parentType.validations + validations
        )
    }

}