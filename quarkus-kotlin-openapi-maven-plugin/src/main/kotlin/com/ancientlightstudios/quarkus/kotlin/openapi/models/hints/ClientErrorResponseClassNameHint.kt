package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the name of the sealed error response interface in the client
object ClientErrorResponseClassNameHint : Hint<ClassName> {

    var OpenApiRequest.clientErrorResponseClassName: ClassName
        get() = get(ClientErrorResponseClassNameHint) ?: ProbableBug("Name of the client error response not set")
        set(value) = set(ClientErrorResponseClassNameHint, value)

}