package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType

class ContentTypeMapper(private val config: Config) {

    fun mapContentType(contentType: String): ContentType {
        val mappedContentType = config.contentTypeFor(contentType) ?: contentType
        return ContentType.fromString(mappedContentType)
    }

}