package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.*

fun SchemaDefinition.getAllProperties(): List<Pair<String, SchemaProperty>> {
    val result = mutableListOf<Pair<String, SchemaProperty>>()

    if (this is ObjectSchemaDefinition) {
        result.addAll(properties)
    } else if (this is AllOfSchemaDefinition) {
        result.addAll(schemas.map { getSchemaDefinition(it) }.flatMap { it.getAllProperties() })
    }

    return result
}

private fun getSchemaDefinition(schema: Schema): SchemaDefinition =
    when (schema) {
        is SchemaDefinition -> schema
        is SchemaReference<*> -> getSchemaDefinition(schema.target)
    }
