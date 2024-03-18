package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

class StringValidationComponent(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null
) : SchemaComponent, MetaComponent