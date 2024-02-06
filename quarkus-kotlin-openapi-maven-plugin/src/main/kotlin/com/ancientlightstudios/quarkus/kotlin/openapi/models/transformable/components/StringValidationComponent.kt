package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

class StringValidationComponent(
    var minLength: Int? = null,
    var maxLength: Int? = null,
    var pattern: String? = null
) : SchemaDefinitionComponent