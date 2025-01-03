package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.HintsAware
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.SchemaContainer

// TODO: encoding in case of multipart/form-data or application/x-www-form-urlencoded
class OpenApiContentMapping(
    var mappedContentType: ContentType,
    var rawContentType: String,
    override var schema: OpenApiSchema
) : HintsAware(), SchemaContainer

