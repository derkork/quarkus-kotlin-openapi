package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the identifier of a bundle
object RequestBundleIdentifierHint : Hint<String> {

    var OpenApiRequestBundle.requestBundleIdentifier: String
        get() = get(RequestBundleIdentifierHint) ?: ProbableBug("Identifier of the request bundle not set")
        set(value) = set(RequestBundleIdentifierHint, value)

}