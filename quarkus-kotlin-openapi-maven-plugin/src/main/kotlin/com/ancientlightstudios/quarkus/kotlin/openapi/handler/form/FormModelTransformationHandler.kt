package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ObjectModelClass
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.ModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationMode
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.unwrapModelClass

class FormModelTransformationHandler : ModelTransformationHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun registerTransformations(
        model: ModelClass, mode: TransformationMode, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationFormUrlencoded) {
        if (model is ObjectModelClass) {
            // explode the form into several parts, which can require different content types
            model.properties.forEach { property ->
                property.model.instance.unwrapModelClass()?.let { modelClass ->
                    installTransformationFeatureFor(modelClass, mode)
                }
            }
        } else {
            // it's just a single item
            installTransformationFeatureFor(model, mode)
        }
    }

    private fun installTransformationFeatureFor(model: ModelClass, mode: TransformationMode) {
        registry.getHandler<ModelTransformationHandler, Unit> {
            registerTransformations(model, mode, model.defaultContentType())
        }
    }

}