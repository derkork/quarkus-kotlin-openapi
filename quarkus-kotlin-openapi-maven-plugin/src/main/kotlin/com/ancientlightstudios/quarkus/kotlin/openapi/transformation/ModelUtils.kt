package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OverlayTargetHint.overlayTarget
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaModeHint.schemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModelHint.schemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiContentMapping
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

fun TransformationContext.contentModelFor(
    content: OpenApiContentMapping, direction: Direction, required: Boolean
): ContentInfo {
    val schemaDirection = when (direction) {
        Direction.Up -> SchemaDirection.UnidirectionalUp
        Direction.Down -> SchemaDirection.UnidirectionalDown
    }
    val model = ModelUsage(modelInstanceFor(content.schema, schemaDirection, required))
    return ContentInfo(model, content.mappedContentType, content.rawContentType)
}

fun TransformationContext.modelInstanceFor(
    schema: OpenApiSchema, direction: SchemaDirection, required: Boolean = true
): ModelInstance {
    val modelSchema = when (schema.schemaMode) {
        SchemaMode.Model -> schema
        SchemaMode.Overlay -> schema.overlayTarget
    }

    return when (val targetModel = modelSchema.schemaTargetModel) {
        SchemaTargetModel.ArrayModel -> arrayModelInstanceFor(schema, modelSchema, direction, required)
        is SchemaTargetModel.EnumModel -> enumModelInstanceFor(schema, modelSchema, direction, required)
        SchemaTargetModel.MapModel -> mapModelInstanceFor(schema, modelSchema, direction, required)
        SchemaTargetModel.ObjectModel -> objectModelInstanceFor(schema, modelSchema, direction, required)
        SchemaTargetModel.OneOfModel -> oneOfModelInstanceFor(schema, modelSchema, direction, required)
        is SchemaTargetModel.PrimitiveTypeModel -> primitiveTypeModelInstanceFor(schema, targetModel, required)
    }
}

private fun TransformationContext.arrayModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val itemsSchema = modelSchema.getComponent<ArrayItemsComponent>()
        ?: ProbableBug("Array schema without items definition. Found in ${modelSchema.originPath}")
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    val validations = schema.getComponent<ValidationComponent>()?.validations ?: listOf()
    return CollectionModelInstance(
        ModelUsage(modelInstanceFor(itemsSchema.schema, direction, true)),
        required,
        nullable,
        validations
    )
}

private fun TransformationContext.enumModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val modelClass = getRegisteredModelClass<EnumModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    val defaultValue = schema.getComponent<DefaultComponent>()?.default
    val validations = schema.getComponent<ValidationComponent>()?.validations ?: listOf()
    return EnumModelInstance(modelClass, defaultValue, required, nullable, validations)
}

private fun TransformationContext.mapModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val itemsSchema = modelSchema.getComponent<MapComponent>()
        ?: ProbableBug("Map schema without items definition. Found in ${modelSchema.originPath}")
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    val validations = schema.getComponent<ValidationComponent>()?.validations ?: listOf()
    return MapModelInstance(
        ModelUsage(modelInstanceFor(itemsSchema.schema, direction, true)),
        required,
        nullable,
        validations
    )
}

private fun TransformationContext.objectModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val modelClass = getRegisteredModelClass<ObjectModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    val validations = schema.getComponent<ValidationComponent>()?.validations ?: listOf()
    return ObjectModelInstance(modelClass, required, nullable, validations)
}

private fun TransformationContext.oneOfModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val modelClass = getRegisteredModelClass<OneOfModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    val validations = schema.getComponent<ValidationComponent>()?.validations ?: listOf()
    return OneOfModelInstance(modelClass, required, nullable, validations)
}

private fun primitiveTypeModelInstanceFor(
    schema: OpenApiSchema,
    targetModel: SchemaTargetModel.PrimitiveTypeModel,
    required: Boolean
): ModelInstance {
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    val defaultValue = schema.getComponent<DefaultComponent>()?.default
    val validations = schema.getComponent<ValidationComponent>()?.validations ?: listOf()
    return PrimitiveTypeModelInstance(targetModel.base, defaultValue, required, nullable, validations)
}
