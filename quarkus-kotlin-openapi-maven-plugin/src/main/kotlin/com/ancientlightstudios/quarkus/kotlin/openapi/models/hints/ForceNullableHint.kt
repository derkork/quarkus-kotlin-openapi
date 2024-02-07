package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ForceNullableHint : Hint<Boolean> {

    var TransformableSchemaUsage.forceNullable: Boolean
        get() = get(ForceNullableHint) ?: false
        set(value) = set(ForceNullableHint, value)

}