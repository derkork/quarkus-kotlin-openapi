package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.ModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationMode

class JsonModelHandler : ModelTransformationHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun installTransformationFeatureFor(
        model: ModelClass, mode: TransformationMode, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationJson) {
        val feature = when (mode) {
            TransformationMode.Serialization -> JsonSerializationFeature
            TransformationMode.Deserialization -> JsonDeserializationFeature
        }

        propagateTransformationFeature(model, feature)
    }

    private fun propagateTransformationFeature(model: ModelClass, feature: Feature) {
        if (!model.features.add(feature)) {
            // this feature was already added
            return
        }

        when (model) {
            is EnumModelClass -> {
                // json deserialization of an enum depends on the plain deserialization, so this feature needs to be added to
                if (feature == JsonDeserializationFeature) {
                    registry.getHandler<ModelTransformationHandler, Unit> { 
                        installTransformationFeatureFor(model, TransformationMode.Deserialization, ContentType.TextPlain)
                    }
                }
                return
            }
            is ObjectModelClass -> {
                model.properties.forEach { propagateTransformationFeature(it.model, feature) }
                model.additionalProperties?.let { propagateTransformationFeature(it, feature) }
            }

            is OneOfModelClass -> {
                model.options.forEach { propagateTransformationFeature(it.model, feature) }
            }
        }
    }

    private fun propagateTransformationFeature(model: ModelUsage, feature: Feature) {
        when (val modelInstance = model.instance) {
            is CollectionModelInstance -> propagateTransformationFeature(modelInstance.items, feature)
            is EnumModelInstance -> propagateTransformationFeature(modelInstance.ref, feature)
            is MapModelInstance -> propagateTransformationFeature(modelInstance.items, feature)
            is ObjectModelInstance -> propagateTransformationFeature(modelInstance.ref, feature)
            is OneOfModelInstance -> propagateTransformationFeature(modelInstance.ref, feature)
            is PrimitiveTypeModelInstance -> return
        }
    }
}