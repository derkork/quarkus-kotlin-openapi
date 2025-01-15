package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiBody

class RequestBody(val name: String, val content: ContentInfo, val source: OpenApiBody) {

    val sourceName = name

}