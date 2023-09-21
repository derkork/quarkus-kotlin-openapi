package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef

class SchemaRegistry {

    private val references = mutableMapOf<String, SchemaRef>()
    private val schemas = mutableMapOf<String, Schema>()

    val resolvedSchemas:Set<Schema>
        get() = schemas.values.toSet()

    fun getOrRegisterReference(ref: String) = references.getOrPut(ref) { SchemaRef(ref, this) }

    fun resolveRef(ref: SchemaRef, schema: Schema) {
        schemas[ref.id] = schema
    }

    fun resolve(ref: SchemaRef) =
        schemas[ref.id] ?: throw IllegalArgumentException("Schema not found: ${ref.id}")

    fun unresolved() = references.filter { !schemas.containsKey(it.key) }.values

}