package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

class NumberValidationComponent(
    var minimum: ComparableNumber? = null,
    var maximum: ComparableNumber? = null
) : SchemaDefinitionComponent

data class ComparableNumber(val value: String, val exclusive: Boolean)