package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the identifier of a request
object RequestIdentifierHint : Hint<String> {

    var OpenApiRequest.requestIdentifier: String
        get() = get(RequestIdentifierHint) ?: ProbableBug("Identifier of the request not set")
        set(value) = set(RequestIdentifierHint, value)

}