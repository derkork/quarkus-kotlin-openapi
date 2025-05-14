package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BaseType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.BasedOnHint.basedOn
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OverlayTargetHint.overlayTarget
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaModeHint.hasSchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaModeHint.schemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModelHint.schemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import org.slf4j.LoggerFactory

class AssignTypeToSchemaRefactoring : SpecRefactoring {

    private val log = LoggerFactory.getLogger(AssignTypeToSchemaRefactoring::class.java)

    private lateinit var config: Config

    override fun RefactoringContext.perform() {
        this@AssignTypeToSchemaRefactoring.config = config
        var remainingSchemas = spec.schemas

        do {
            val (ready, notReady) = remainingSchemas.partition(::isSchemaReady)
            remainingSchemas = notReady

            if (ready.isEmpty()) {
                // there are no more schemas to process
                break
            }

            ready.forEach(::processSchema)
        } while (true)

        if (remainingSchemas.isNotEmpty()) {
            log.warn("The following schemas can't be converted into types")
            remainingSchemas.forEach {
                log.warn("- ${it.originPath}")
            }

            ProbableBug("Can't process target types for some schemas")
        }
    }

    private fun isSchemaReady(schema: OpenApiSchema): Boolean {
        // schema is ready if all base schemas are already processed (or if there is no base schema at all)
        return schema.basedOn
            .all { it.hasSchemaMode() }
    }

    private fun processSchema(schema: OpenApiSchema) {
        val basedOn = schema.basedOn

        // this schema is a real model, if there is no base schema set
        if (basedOn.isEmpty()) {
            makeModel(schema)
            return
        }

        val overlayTarget = basedOn
            // in case this is just an overlay, get the real model
            .map { it.unwrapOverlay() }
            .firstOrNull { structurallyIdentical(schema, it) }

        when (overlayTarget) {
            null -> makeModel(schema)
            else -> makeOverlay(schema, overlayTarget)
        }
    }

    private fun makeModel(schema: OpenApiSchema) {
        schema.schemaTargetModel = targetModelFor(schema)
        schema.schemaMode = SchemaMode.Model
    }

    private fun makeOverlay(schema: OpenApiSchema, base: OpenApiSchema) {
        // as this is an overlay it shares the same structure as the given parent. However, it still can have
        // differences in its meta components. Remove all structural components as these are no longer needed
        schema.components = schema.components.filterNot { it is StructuralComponent }

        schema.overlayTarget = base
        schema.schemaMode = SchemaMode.Overlay
    }

    // the target of an overlay is always a real model
    private fun OpenApiSchema.unwrapOverlay(): OpenApiSchema = when (this.schemaMode) {
        SchemaMode.Overlay -> this.overlayTarget
        else -> this
    }

    private fun structurallyIdentical(current: OpenApiSchema, base: OpenApiSchema): Boolean {
        val currentModel = targetModelFor(current)

        // type of both schemas must be identical
        if (!currentModel.compatibleWith(base.schemaTargetModel)) {
            return false
        }

        return when (currentModel) {
            is SchemaTargetModel.ArrayModel -> structurallyIdenticalArray(current, base)
            is SchemaTargetModel.EnumModel -> structurallyIdenticalEnum(current, base)
            SchemaTargetModel.MapModel -> structurallyIdenticalMap(current, base)
            SchemaTargetModel.ObjectModel -> structurallyIdenticalObject(current, base)
            SchemaTargetModel.OneOfModel -> structurallyIdenticalOneOf(current, base)
            // in case of a primitive type, if the target model is compatible, everything is fine
            is SchemaTargetModel.PrimitiveTypeModel -> true
        }
    }

    private fun structurallyIdenticalArray(current: OpenApiSchema, base: OpenApiSchema): Boolean {
        val currentItems = current.getComponent<ArrayItemsComponent>()?.schema?.originPath
        val baseItems = base.getComponent<ArrayItemsComponent>()?.schema?.originPath

        // arrays are compatible, if the items are equal
        return currentItems == baseItems
    }

    private fun structurallyIdenticalEnum(current: OpenApiSchema, base: OpenApiSchema): Boolean {
        val currentItems = current.getComponent<EnumValidationComponent>()?.values ?: listOf()
        val baseItems = base.getComponent<EnumValidationComponent>()?.values ?: listOf()

        // enums are not compatible, if they have different items
        if (currentItems.size != baseItems.size || !currentItems.containsAll(baseItems)) {
            return false
        }

        val currentItemNames = current.getComponent<EnumItemNamesComponent>()?.values ?: mapOf()
        val baseItemNames = base.getComponent<EnumItemNamesComponent>()?.values ?: mapOf()

        // enums are not compatible, if item names are different
        return currentItemNames.size == baseItemNames.size && currentItemNames.all { baseItemNames[it.key] == it.value }
    }

    private fun structurallyIdenticalMap(current: OpenApiSchema, base: OpenApiSchema): Boolean {
        val currentItems = current.getComponent<MapComponent>()?.schema?.originPath
        val baseItems = base.getComponent<MapComponent>()?.schema?.originPath

        // maps are compatible, if the items are equal
        return currentItems == baseItems
    }

    private fun structurallyIdenticalObject(current: OpenApiSchema, base: OpenApiSchema): Boolean {
        // TODO: we should check the property schema too. But this is difficult for recursive and enhanced properties (merge with allOf)
        val currentProperties = current.getComponent<ObjectComponent>()?.properties?.map { it.name } ?: listOf()
        val baseProperties = base.getComponent<ObjectComponent>()?.properties?.map { it.name } ?: listOf()

        // objects are not compatible, if they have different properties
        if (currentProperties.size != baseProperties.size || !currentProperties.containsAll(baseProperties)) {
            return false
        }

        val currentRequired = current.getComponent<ObjectValidationComponent>()?.required ?: listOf()
        val baseRequired = base.getComponent<ObjectValidationComponent>()?.required ?: listOf()

        // objects are not compatible, if they have different required properties
        if (currentRequired.size != baseRequired.size || !currentRequired.containsAll(baseRequired)) {
            return false
        }

        // in case one of them has additional properties, check them too
        return structurallyIdenticalMap(current, base)
    }

    private fun structurallyIdenticalOneOf(current: OpenApiSchema, base: OpenApiSchema): Boolean {
        val currentOneOf = current.getComponent<OneOfComponent>()
        val baseOneOf = base.getComponent<OneOfComponent>()

        val currentOptions = currentOneOf?.options?.map { it.schema.originPath } ?: listOf()
        val baseOptions = baseOneOf?.options?.map { it.schema.originPath } ?: listOf()

        // oneOfs are not compatible, if they have different options
        if (currentOptions.size != baseOptions.size || !currentOptions.containsAll(baseOptions)) {
            return false
        }

        val currentProperty = currentOneOf?.discriminator?.property
        val baseProperty = baseOneOf?.discriminator?.property

        // oneOfs are not compatible, if they have different discriminator properties
        if (currentProperty != baseProperty) {
            return false
        }

        val currentMapping = currentOneOf?.discriminator?.additionalMappings ?: mapOf()
        val baseMapping = baseOneOf?.discriminator?.additionalMappings ?: mapOf()

        // oneOfs are not compatible, if mappings are different
        return currentMapping.size == baseMapping.size && currentMapping.all { baseMapping[it.key] == it.value }
    }

    private fun targetModelFor(schema: OpenApiSchema): SchemaTargetModel {
        val type = schema.getComponent<TypeComponent>()?.type ?: SchemaType.Object
        val format = schema.getComponent<FormatComponent>()?.format

        if (schema.hasComponent<OneOfComponent>()) {
            return SchemaTargetModel.OneOfModel
        }

        if (type == SchemaType.Array || schema.hasComponent<ArrayItemsComponent>()) {
            return SchemaTargetModel.ArrayModel
        }

        if (type == SchemaType.Object) {
            val hasMap = schema.hasComponent<MapComponent>()
            val hasProperties = schema.hasComponent<ObjectComponent>()

            if (hasMap && !hasProperties) {
                return SchemaTargetModel.MapModel
            }

            return SchemaTargetModel.ObjectModel
        }

        val baseType = baseTypeFor(type, format)

        if (schema.hasComponent<EnumValidationComponent>()) {
            return SchemaTargetModel.EnumModel(baseType)
        }

        return SchemaTargetModel.PrimitiveTypeModel(baseType)
    }

    private fun baseTypeFor(type: SchemaType, format: String?): BaseType = when (type) {
        SchemaType.String -> mapStringType(format)
        SchemaType.Number -> mapNumberType(format)
        SchemaType.Integer -> mapIntegerType(format)
        SchemaType.Boolean -> mapBooleanType(format)
        else -> ProbableBug("unsupported primitive type mapping '$type' with format '$format'")
    }

    private fun mapCustomType(type: SchemaType, format: String?, fallback: BaseType): BaseType {
        if (format.isNullOrEmpty()) {
            return fallback
        }

        // TODO: support generic types
        val result = config.typeNameFor(type.value, format) ?: return fallback
        return BaseType.Custom(result.name, result.packageName)
    }

    private fun mapStringType(format: String?) = when (format) {
        "byte",
        "binary" -> BaseType.ByteArray

        else -> mapCustomType(SchemaType.String, format, BaseType.String)
    }

    private fun mapNumberType(format: String?) = when (format) {
        "float" -> BaseType.Float
        "double" -> BaseType.Double
        "int32" -> BaseType.Int
        "int64" -> BaseType.Long
        "uint16", "uint32" -> BaseType.UInt
        "uint64" -> BaseType.ULong
        else -> mapCustomType(SchemaType.Number, format, BaseType.BigDecimal)
    }

    private fun mapIntegerType(format: String?) = when (format) {
        "int32" -> BaseType.Int
        "int64" -> BaseType.Long
        "uint16", "uint32" -> BaseType.UInt
        "uint64" -> BaseType.ULong
        else -> mapCustomType(SchemaType.Integer, format, BaseType.BigInteger)
    }

    private fun mapBooleanType(format: String?) = mapCustomType(SchemaType.Boolean, format, BaseType.Boolean)

}