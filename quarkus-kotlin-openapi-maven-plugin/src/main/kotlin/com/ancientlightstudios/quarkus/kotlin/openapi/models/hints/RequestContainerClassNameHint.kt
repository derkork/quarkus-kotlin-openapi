package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object RequestContainerClassNameHint : Hint<ClassName> {

    var TransformableRequest.requestContainerClassName: ClassName
        get() = get(RequestContainerClassNameHint) ?: ProbableBug("Name of the request container not set")
        set(value) = set(RequestContainerClassNameHint, value)

}