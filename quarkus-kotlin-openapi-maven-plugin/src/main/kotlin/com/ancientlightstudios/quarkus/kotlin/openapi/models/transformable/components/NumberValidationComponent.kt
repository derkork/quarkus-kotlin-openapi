package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

class NumberValidationComponent(
    val minimum: ComparableNumber? = null,
    val maximum: ComparableNumber? = null
) : SchemaDefinitionComponent, MetaComponent

data class ComparableNumber(val value: String, val exclusive: Boolean)