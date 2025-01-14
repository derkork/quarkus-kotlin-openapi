package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelClass
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.ModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationMode

class FormModelHandler : ModelTransformationHandler {

    override fun installTransformationFeatureFor(
        model: ModelClass, mode: TransformationMode, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationFormUrlencoded) {
        // TODO
    }

}