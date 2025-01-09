package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType

data class ContentInfo(val model: ModelUsage, val contentType: ContentType, val rawContentType: String)
