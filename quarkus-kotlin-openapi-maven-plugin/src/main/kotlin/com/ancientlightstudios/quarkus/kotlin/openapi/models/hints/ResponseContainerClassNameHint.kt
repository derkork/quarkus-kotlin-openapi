package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ResponseContainerClassNameHint : Hint<ClassName> {

    var TransformableRequest.responseContainerClassName: ClassName
        get() = get(ResponseContainerClassNameHint) ?: ProbableBug("Name of the response container not set")
        set(value) = set(ResponseContainerClassNameHint, value)

}