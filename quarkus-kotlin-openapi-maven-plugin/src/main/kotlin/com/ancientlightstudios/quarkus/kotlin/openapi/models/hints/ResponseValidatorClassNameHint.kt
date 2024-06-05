package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the class name of the response validator
object ResponseValidatorClassNameHint : Hint<ClassName> {

    var TransformableRequest.responseValidatorClassName: ClassName
        get() = get(ResponseValidatorClassNameHint) ?: ProbableBug("Name of the response validator not set")
        set(value) = set(ResponseValidatorClassNameHint, value)

}