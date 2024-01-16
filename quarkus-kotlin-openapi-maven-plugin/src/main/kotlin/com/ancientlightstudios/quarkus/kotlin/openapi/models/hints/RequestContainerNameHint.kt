package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object RequestContainerNameHint : Hint<ClassName> {

    var TransformableRequest.requestContainerName: ClassName
        get() = get(RequestContainerNameHint) ?: ProbableBug("Name of the request container not set")
        set(value) = set(RequestContainerNameHint, value)

}