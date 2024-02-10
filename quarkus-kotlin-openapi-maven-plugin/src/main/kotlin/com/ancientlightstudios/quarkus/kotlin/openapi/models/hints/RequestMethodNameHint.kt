package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the method name of a request
object RequestMethodNameHint : Hint<MethodName> {

    var TransformableRequest.requestMethodName: MethodName
        get() = get(RequestMethodNameHint) ?: ProbableBug("Name of the request not set")
        set(value) = set(RequestMethodNameHint, value)

}