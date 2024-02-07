package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object TypeUsageHint : Hint<TypeUsage> {

    var TransformableSchemaUsage.typeUsage: TypeUsage
        get() = get(TypeUsageHint) ?: ProbableBug("No type set for schema")
        set(value) = set(TypeUsageHint, value)

}