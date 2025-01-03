package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.ToBeChecked
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

@ToBeChecked
// knows how to create or extend a simple primitive type (without any *Of stuff)
class CreateSimplePrimitiveTypeRefactoring(
    private val typeMapper: TypeMapper,
    private val schema: OpenApiSchema,
    private val parentType: TypeDefinition? = null,
) : SpecRefactoring {

    override fun RefactoringContext.perform() {
//        val type = schema.typeComponent()?.type
//        val nullable = schema.nullableComponent()?.nullable
//        val modifier = schema.schemaModifierComponent()?.modifier
//        val validations = schema.validationComponents().map { it.validation }
//
//        val typeDefinition = when (parentType) {
//            null -> createNewType(type, nullable, modifier, validations)
//            is PrimitiveTypeDefinition -> createOverlayTypeForSimplePrimitive(
//                parentType, type, nullable, modifier, validations
//            )
//
//            is EnumTypeDefinition -> createOverlayTypeForEnum(parentType, type, nullable, modifier, validations)
//            else -> ProbableBug("incompatible base type for a primitive overlay")
//        }
//
//        schema.typeDefinition = typeDefinition
//    }
//
//    private fun RefactoringContext.createNewType(
//        type: SchemaTypes?,
//        nullable: Boolean?,
//        modifier: SchemaModifier?,
//        validations: List<SchemaValidation>
//    ): TypeDefinition {
//        return when (type) {
//            SchemaTypes.String,
//            SchemaTypes.Number,
//            SchemaTypes.Integer,
//            SchemaTypes.Boolean -> generatePrimitiveType(type, nullable ?: false, modifier, validations)
//
//            else -> ProbableBug("Incompatible type $type for a primitive type")
//        }
//    }
//
//    private fun RefactoringContext.generatePrimitiveType(
//        type: SchemaTypes,
//        nullable: Boolean,
//        modifier: SchemaModifier?,
//        validations: List<SchemaValidation>
//    ): TypeDefinition {
//        val format = schema.formatComponent()?.format
//
//        val baseType = typeMapper.mapToPrimitiveType(type, format)
//        val default = schema.defaultComponent()?.default
//
//        return when (val enumComponent = schema.enumValidationComponent()) {
//            null -> RealPrimitiveTypeDefinition(baseType, nullable, modifier, default, validations)
//            else -> generateEnumType(baseType, nullable, modifier, default, enumComponent, validations)
//        }
//    }
//
//    private fun RefactoringContext.generateEnumType(
//        baseType: ClassName,
//        nullable: Boolean,
//        modifier: SchemaModifier?,
//        default: String?,
//        enumComponent: EnumValidationComponent,
//        validations: List<SchemaValidation>
//    ): TypeDefinition {
//        val enumNames = schema.enumItemNamesComponent()?.values ?: mapOf()
//
//        val items = enumComponent.values.map {
//            // use the given name as it is otherwise try to create a name out of the item value
//            val constantName = enumNames[it]?.rawConstantName() ?: it.constantName()
//            EnumTypeItem(it, constantName, baseType.literalFor(it))
//        }
//
//        return RealEnumTypeDefinition(
//            schema.name.value.className(modelPackage),
//            baseType, nullable, modifier, items, default, validations
//        )
//    }
//
//    private fun RefactoringContext.createOverlayTypeForSimplePrimitive(
//        parentType: PrimitiveTypeDefinition,
//        type: SchemaTypes?,
//        nullable: Boolean?,
//        modifier: SchemaModifier?,
//        validations: List<SchemaValidation>
//    ): TypeDefinition {
//        if (modifier != null && parentType.modifier != null && modifier != parentType.modifier) {
//            ProbableBug("schema ${schema.originPath} has different readonly/write-only modifier than it's base schema")
//        }
//
//        // check if we have to override the default value
//        val default = schema.defaultComponent()?.default
//        val enumItems = schema.enumValidationComponent()?.values ?: listOf()
//
//        if (enumItems.isEmpty()) {
//            // just an overlay
//            return PrimitiveTypeDefinitionOverlay(
//                parentType, nullable == true, modifier ?: parentType.modifier, default, validations
//            )
//        }
//
//        // we have to build an enum type out of it
//        val enumNames = schema.enumItemNamesComponent()?.values ?: mapOf()
//
//        val items = enumItems.map {
//            // use the given name as it is otherwise try to create a name out of the item value
//            val constantName = enumNames[it]?.rawConstantName() ?: it.constantName()
//            EnumTypeItem(it, constantName, parentType.baseType.literalFor(it))
//        }
//
//        return RealEnumTypeDefinition(
//            schema.name.value.className(modelPackage),
//            parentType.baseType,
//            nullable == true || parentType.nullable,
//            modifier ?: parentType.modifier,
//            items,
//            default ?: parentType.defaultValue,
//            parentType.validations + validations
//        )
//    }
//
//    private fun RefactoringContext.createOverlayTypeForEnum(
//        parentType: EnumTypeDefinition,
//        type: SchemaTypes?,
//        nullable: Boolean?,
//        modifier: SchemaModifier?,
//        validations: List<SchemaValidation>
//    ): TypeDefinition {
//        // TODO: if type and/or format are redefined, the type mapper must return the same class name,
//        //  otherwise it's not compatible and we should fail
//
//        if (modifier != null && parentType.modifier != null && modifier != parentType.modifier) {
//            ProbableBug("schema ${schema.originPath} has different readonly/write-only modifier than it's base schema")
//        }
//
//        // check if we have to override the default value
//        val default = schema.defaultComponent()?.default
//
//        val parentItems = parentType.items
//        val enumItems = schema.enumValidationComponent()?.values ?: listOf()
//        val (newEnumItems, redefinedEnumItems) = enumItems.partition { current -> parentItems.none { it.sourceName == current } }
//
//        // enum structure is still the same, we can just create an overlay
//        if (newEnumItems.isEmpty()) {
//            return EnumTypeDefinitionOverlay(parentType, nullable == true, modifier, default, validations)
//        }
//
//        // create a new list of items. The order is: everything defined/redefined here and then the stuff only defined at the parent
//        val enumNames = schema.enumItemNamesComponent()?.values ?: mapOf()
//        val newItems = enumItems.map { item ->
//            if (newEnumItems.contains(item)) {
//                // it's new. we support custom names for this
//                // use the given name as it is otherwise try to create a name out of the item value
//                val constantName = enumNames[item]?.rawConstantName() ?: item.constantName()
//                EnumTypeItem(item, constantName, parentType.baseType.literalFor(item))
//            } else {
//                // redefined. use it as declared by the parent. maybe position changed
//                parentItems.first { it.sourceName == item }
//            }
//        }
//            // add everything from the parent, that was not redefined
//            .plus(parentType.items.filterNot { redefinedEnumItems.contains(it.sourceName) })
//
//        // new items available, we have to create a new enum
//        return RealEnumTypeDefinition(
//            schema.name.value.className(modelPackage),
//            parentType.baseType,
//            nullable == true || parentType.nullable,
//            modifier ?: parentType.modifier,
//            newItems,
//            default ?: parentType.defaultValue,
//            parentType.validations + validations
//        )
    }

}