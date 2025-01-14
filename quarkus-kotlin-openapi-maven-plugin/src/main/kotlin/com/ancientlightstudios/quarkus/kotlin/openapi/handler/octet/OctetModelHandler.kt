package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelClass
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.ModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationMode

class OctetModelHandler : ModelTransformationHandler {

    override fun installTransformationFeatureFor(
        model: ModelClass, mode: TransformationMode, contentType: ContentType
        // we still have to check if we are allowed/required to act
    ) = contentType.matches(ContentType.ApplicationOctetStream) {
        // nothing to do
    }

}