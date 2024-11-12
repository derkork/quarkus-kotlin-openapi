package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the name of the sealed http response class in the client
object ClientHttpResponseClassNameHint : Hint<ClassName> {

    var OpenApiRequest.clientHttpResponseClassName: ClassName
        get() = get(ClientHttpResponseClassNameHint) ?: ProbableBug("Name of the client http response not set")
        set(value) = set(ClientHttpResponseClassNameHint, value)

}