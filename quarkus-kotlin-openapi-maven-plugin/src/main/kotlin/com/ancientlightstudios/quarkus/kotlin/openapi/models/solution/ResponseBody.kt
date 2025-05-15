package com.ancientlightstudios.quarkus.kotlin.openapi.models.solution

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiBody

class ResponseBody(
    val name: String,
    val content: ContentInfo,
    val source: OpenApiBody,
    val context: String = "response.$name"
) {

    val sourceName = name

}