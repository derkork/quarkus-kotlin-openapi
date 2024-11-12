package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiContentMapping
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the type usage for a parameter, body, etc
object TypeUsageHint : Hint<TypeUsage> {

    var OpenApiContentMapping.typeUsage: TypeUsage
        get() = get(TypeUsageHint) ?: ProbableBug("No type assigned to body content")
        set(value) = set(TypeUsageHint, value)

}