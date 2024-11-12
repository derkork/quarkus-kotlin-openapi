package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the class name of the container that contains all the input parameter for a request
object RequestContainerClassNameHint : Hint<ClassName> {

    var OpenApiRequest.requestContainerClassName: ClassName
        get() = get(RequestContainerClassNameHint) ?: ProbableBug("Name of the request container not set")
        set(value) = set(RequestContainerClassNameHint, value)

}