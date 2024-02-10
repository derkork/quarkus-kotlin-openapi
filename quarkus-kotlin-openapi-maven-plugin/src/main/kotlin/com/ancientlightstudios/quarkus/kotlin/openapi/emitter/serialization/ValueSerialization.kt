package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType

fun ContentType.getSerializationMethod() = when (this) {
    ContentType.ApplicationJson -> "asJsonNode".rawMethodName()
    ContentType.TextPlain -> "foooo".rawMethodName()
    ContentType.MultipartFormData -> "foooo".rawMethodName()
    ContentType.ApplicationFormUrlencoded -> "foooo".rawMethodName()
    ContentType.ApplicationOctetStream -> "foooo".rawMethodName()
}