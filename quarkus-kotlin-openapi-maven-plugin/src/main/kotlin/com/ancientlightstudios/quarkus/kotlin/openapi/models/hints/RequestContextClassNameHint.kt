package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the class name of the container that contains the request context
object RequestContextClassNameHint : Hint<ClassName> {

    var TransformableRequest.requestContextClassName: ClassName
        get() = get(RequestContextClassNameHint) ?: ProbableBug("Name of the request context not set")
        set(value) = set(RequestContextClassNameHint, value)

}