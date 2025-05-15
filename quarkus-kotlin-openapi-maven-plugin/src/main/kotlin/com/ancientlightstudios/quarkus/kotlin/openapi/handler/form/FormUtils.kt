package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

// TODO: this is just temporary and can be replaced when we support encoding for OpenAPI content
fun ModelUsage.defaultContentType() = when (instance) {
    is CollectionModelInstance,
    is EnumModelInstance,
    is PrimitiveTypeModelInstance -> ContentType.TextPlain

    else -> ContentType.ApplicationJson
}

fun ModelClass.defaultContentType() = when (this) {
    is EnumModelClass -> ContentType.TextPlain
    is ObjectModelClass -> ContentType.ApplicationJson
    is OneOfModelClass -> ContentType.ApplicationJson
}