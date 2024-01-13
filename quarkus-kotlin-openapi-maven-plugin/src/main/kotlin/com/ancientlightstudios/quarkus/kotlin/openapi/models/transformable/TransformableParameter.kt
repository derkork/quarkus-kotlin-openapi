package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

sealed class TransformableParameter(
    var name: String,
    var required: Boolean
) : TransformableObject()

class TransformablePathParameter(name: String) : TransformableParameter(name, true)
class TransformableQueryParameter(name: String, required: Boolean) : TransformableParameter(name, required)
class TransformableHeaderParameter(name: String, required: Boolean) : TransformableParameter(name, required)
class TransformableCookieParameter(name: String, required: Boolean) : TransformableParameter(name, required)