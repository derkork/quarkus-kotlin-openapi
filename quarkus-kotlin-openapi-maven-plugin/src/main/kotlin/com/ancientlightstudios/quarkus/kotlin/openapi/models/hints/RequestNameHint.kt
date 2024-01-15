package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object RequestNameHint : Hint<MethodName> {

    var TransformableObject.requestName: MethodName
        get() = get(RequestNameHint) ?: ProbableBug("Name of the request not set")
        set(value) = set(RequestNameHint, value)

}