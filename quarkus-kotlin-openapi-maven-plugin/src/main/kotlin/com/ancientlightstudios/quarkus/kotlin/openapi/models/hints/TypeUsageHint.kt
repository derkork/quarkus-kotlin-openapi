package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableContentMapping
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the type usage for a parameter, body, etc
object TypeUsageHint : Hint<TypeUsage> {

    var TransformableParameter.typeUsage: TypeUsage
        get() = get(TypeUsageHint) ?: ProbableBug("No type assigned to parameter")
        set(value) = set(TypeUsageHint, value)

    var TransformableContentMapping.typeUsage: TypeUsage
        get() = get(TypeUsageHint) ?: ProbableBug("No type assigned to body content")
        set(value) = set(TypeUsageHint, value)


}