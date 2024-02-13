package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the name of the sealed error response interface in the client
object ClientErrorResponseClassNameHint : Hint<ClassName> {

    var TransformableRequest.clientErrorResponseClassName: ClassName
        get() = get(ClientErrorResponseClassNameHint) ?: ProbableBug("Name of the client error response not set")
        set(value) = set(ClientErrorResponseClassNameHint, value)

}