package com.ancientlightstudios.quarkus.kotlin.openapi.builder

import com.ancientlightstudios.quarkus.kotlin.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.SchemaRef

class SchemaRegistry {

    private val references = mutableMapOf<String, SchemaRef>()
    private val schemas = mutableMapOf<String, Schema>()

    val resolvedSchemas:Set<Schema>
        get() = schemas.values.toSet()

    fun getOrRegisterReference(ref: String) = references.getOrPut(ref) { SchemaRef(ref, this) }

    fun getOrRegisterType(type: String) = references.getOrPut(type) {
        schemas[type] = Schema.PrimitiveTypeSchema(type)
        SchemaRef(type, this)
    }

    fun resolveRef(ref: String, schema: Schema) {
        schemas[ref] = schema
    }

    fun resolve(ref: SchemaRef) =
        schemas[ref.id] ?: throw IllegalArgumentException("Schema not found: ${ref.id}")

    fun unresolved() = references.filter { !schemas.containsKey(it.key) }.values

}