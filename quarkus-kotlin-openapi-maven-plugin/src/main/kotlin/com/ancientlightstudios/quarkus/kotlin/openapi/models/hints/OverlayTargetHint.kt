package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaModeHint.hasSchemaMode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the base schema of an overlay (schema with schema type set to SchemaType.Overlay).
// the specified schema itself is never an overlay
object OverlayTargetHint : Hint<OpenApiSchema> {

    var OpenApiSchema.overlayTarget: OpenApiSchema
        get() = get(OverlayTargetHint) ?: ProbableBug("Overlay target not set")
        set(value) {
            if (!value.hasSchemaMode(SchemaMode.Model)) {
                ProbableBug("Overlay target is invalid")
            }

            set(OverlayTargetHint, value)
        }

}
