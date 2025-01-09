package com.ancientlightstudios.quarkus.kotlin.openapi.handler

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType

interface ContentTypeHandler : Handler {

    val supportedContentType: ContentType

}