package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OverlayTargetHint.overlayTarget
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaModeHint.schemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaTargetModelHint.schemaTargetModel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.ArrayItemsComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.MapComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.NullableComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

fun TransformationContext.modelUsageFor(
    schema: OpenApiSchema, direction: SchemaDirection, required: Boolean = true
): ModelUsage {
    val modelSchema = when (schema.schemaMode) {
        SchemaMode.Model -> schema
        SchemaMode.Overlay -> schema.overlayTarget
    }

    return when (val targetModel = modelSchema.schemaTargetModel) {
        SchemaTargetModel.ArrayModel -> arrayModelUsageFor(schema, modelSchema, direction, required)
        is SchemaTargetModel.EnumModel -> enumModelUsageFor(schema, modelSchema, direction, required)
        SchemaTargetModel.MapModel -> mapModelUsageFor(schema, modelSchema, direction, required)
        SchemaTargetModel.ObjectModel -> objectModelUsageFor(schema, modelSchema, direction, required)
        SchemaTargetModel.OneOfModel -> oneOfModelUsageFor(schema, modelSchema, direction, required)
        is SchemaTargetModel.PrimitiveTypeModel -> primitiveTypeModelUsageFor(schema, targetModel, required)
    }
}

private fun TransformationContext.arrayModelUsageFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelUsage {
    val itemsSchema = modelSchema.getComponent<ArrayItemsComponent>()
        ?: ProbableBug("Array schema without items definition. Found in ${modelSchema.originPath}")
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return CollectionModelUsage(modelUsageFor(itemsSchema.schema, direction, true), required, nullable)
}

private fun TransformationContext.enumModelUsageFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelUsage {
    val modelClass = getRegisteredModelClass<EnumModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return EnumModelUsage(modelClass, required, nullable)
}

private fun TransformationContext.mapModelUsageFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelUsage {
    val itemsSchema = modelSchema.getComponent<MapComponent>()
        ?: ProbableBug("Map schema without items definition. Found in ${modelSchema.originPath}")
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return MapModelUsage(modelUsageFor(itemsSchema.schema, direction, true), required, nullable)
}

private fun TransformationContext.objectModelUsageFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelUsage {
    val modelClass = getRegisteredModelClass<ObjectModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return ObjectModelUsage(modelClass, required, nullable)
}

private fun TransformationContext.oneOfModelUsageFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelUsage {
    val modelClass = getRegisteredModelClass<OneOfModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return OneOfModelUsage(modelClass, required, nullable)
}

private fun primitiveTypeModelUsageFor(
    schema: OpenApiSchema,
    targetModel: SchemaTargetModel.PrimitiveTypeModel,
    required: Boolean
): ModelUsage {
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return PrimitiveTypeModelUsage(targetModel.base, required, nullable)
}
