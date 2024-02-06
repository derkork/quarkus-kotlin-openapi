package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody

class BodyInspection(val body: TransformableBody) {

    // checked by the validation, that there is exactly one content type
    fun content(block: ContentInspection.() -> Unit) = ContentInspection(body.content.first()).block()

}