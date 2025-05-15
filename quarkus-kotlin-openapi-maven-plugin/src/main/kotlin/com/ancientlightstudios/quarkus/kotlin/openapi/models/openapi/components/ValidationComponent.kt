package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components

class ValidationComponent(val validations: List<SchemaValidation>) : SchemaComponent, MetaComponent {

    override fun merge(other: List<SchemaComponent>, origin: String): Pair<SchemaComponent, List<SchemaComponent>> {
        val (otherMergeComponents, remainingComponents) = other.partitionIsInstance<ValidationComponent>()

        // just append everything into a single list
        val result = validations.toMutableList()
        otherMergeComponents.flatMapTo(result) { it.validations }
        return ValidationComponent(result) to remainingComponents
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