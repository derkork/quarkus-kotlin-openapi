package com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelClass
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.ModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationMode

class PlainModelHandler : ModelTransformationHandler {

    override fun installTransformationFeatureFor(
        model: ModelClass, mode: TransformationMode, contentType: ContentType
    ) = contentType.matches(ContentType.TextPlain) {
        model.features += when (mode) {
            TransformationMode.Serialization -> PlainSerializationFeature
            TransformationMode.Deserialization -> PlainDeserializationFeature
        }
    }

}