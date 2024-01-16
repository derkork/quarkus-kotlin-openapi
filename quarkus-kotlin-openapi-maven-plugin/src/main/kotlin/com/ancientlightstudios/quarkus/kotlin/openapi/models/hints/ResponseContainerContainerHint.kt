package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ResponseContainerContainerHint : Hint<ClassName> {

    var TransformableRequest.responseContainerName: ClassName
        get() = get(ResponseContainerContainerHint) ?: ProbableBug("Name of the response container not set")
        set(value) = set(ResponseContainerContainerHint, value)

}