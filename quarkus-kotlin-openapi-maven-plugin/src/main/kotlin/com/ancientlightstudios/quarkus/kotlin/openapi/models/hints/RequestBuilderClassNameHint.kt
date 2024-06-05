package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the class name of the builder for a request
object RequestBuilderClassNameHint : Hint<ClassName> {

    var TransformableRequest.requestBuilderClassName: ClassName
        get() = get(RequestBuilderClassNameHint) ?: ProbableBug("Name of the request builder not set")
        set(value) = set(RequestBuilderClassNameHint, value)

}