package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

// a copy of all base schema components to preserve the relationship between schemas after inlining all the components
object BasedOnHint : Hint<List<OpenApiSchema>> {

    var OpenApiSchema.basedOn: List<OpenApiSchema>
        get() = get(BasedOnHint) ?: listOf()
        set(value) = set(BasedOnHint, value)

}