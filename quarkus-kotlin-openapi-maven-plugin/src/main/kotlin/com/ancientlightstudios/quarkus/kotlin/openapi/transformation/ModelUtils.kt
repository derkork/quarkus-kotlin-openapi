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
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.ArrayItemsComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.DefaultComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.MapComponent
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.NullableComponent
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
    return CollectionModelInstance(modelInstanceFor(itemsSchema.schema, direction, true), required, nullable)
}

private fun TransformationContext.enumModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val modelClass = getRegisteredModelClass<EnumModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    val defaultValue = schema.getComponent<DefaultComponent>()?.default
    return EnumModelInstance(modelClass, defaultValue, required, nullable)
}

private fun TransformationContext.mapModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val itemsSchema = modelSchema.getComponent<MapComponent>()
        ?: ProbableBug("Map schema without items definition. Found in ${modelSchema.originPath}")
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return MapModelInstance(modelInstanceFor(itemsSchema.schema, direction, true), required, nullable)
}

private fun TransformationContext.objectModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val modelClass = getRegisteredModelClass<ObjectModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return ObjectModelInstance(modelClass, required, nullable)
}

private fun TransformationContext.oneOfModelInstanceFor(
    schema: OpenApiSchema, modelSchema: OpenApiSchema, direction: SchemaDirection, required: Boolean
): ModelInstance {
    val modelClass = getRegisteredModelClass<OneOfModelClass>(modelSchema, direction)
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    return OneOfModelInstance(modelClass, required, nullable)
}

private fun primitiveTypeModelInstanceFor(
    schema: OpenApiSchema,
    targetModel: SchemaTargetModel.PrimitiveTypeModel,
    required: Boolean
): ModelInstance {
    val nullable = schema.getComponent<NullableComponent>()?.nullable ?: false
    val defaultValue = schema.getComponent<DefaultComponent>()?.default
    return PrimitiveTypeModelInstance(targetModel.base, defaultValue, required, nullable)
}
