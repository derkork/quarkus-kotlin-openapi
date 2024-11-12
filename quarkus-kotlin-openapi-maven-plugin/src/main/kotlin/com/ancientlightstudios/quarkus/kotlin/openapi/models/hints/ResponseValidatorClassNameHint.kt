package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequest
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the class name of the response validator
object ResponseValidatorClassNameHint : Hint<ClassName> {

    var OpenApiRequest.responseValidatorClassName: ClassName
        get() = get(ResponseValidatorClassNameHint) ?: ProbableBug("Name of the response validator not set")
        set(value) = set(ResponseValidatorClassNameHint, value)

}