package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

// specifies whether a schema needs to be split, because it has different constraints for up and down direction
object SplitFlagHint : Hint<Boolean> {

    fun OpenApiSchema.hasSplitFlag() = has(SplitFlagHint)

    fun OpenApiSchema.setSplitFlag() {
        set(SplitFlagHint, true)
    }

}