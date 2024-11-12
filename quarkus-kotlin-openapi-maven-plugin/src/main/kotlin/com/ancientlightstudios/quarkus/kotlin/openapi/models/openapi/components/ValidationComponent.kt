package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema

class ValidationComponent(val validation: SchemaValidation) : SchemaComponent, MetaComponent {

    companion object {

        fun OpenApiSchema.validationComponents() = components.filterIsInstance<ValidationComponent>()

    }

}

sealed interface SchemaValidation

class ArrayValidation(val minSize: Int? = null, val maxSize: Int? = null) : SchemaValidation

class StringValidation(val minLength: Int? = null, val maxLength: Int? = null, val pattern: String? = null) :
    SchemaValidation

class NumberValidation(val minimum: ComparableNumber? = null, val maximum: ComparableNumber? = null) : SchemaValidation

data class ComparableNumber(val value: String, val exclusive: Boolean)

class CustomConstraintsValidation(val constraints: List<String>) : SchemaValidation

class PropertiesValidation(val minProperties: Int? = null, val maxProperties: Int? = null) : SchemaValidation