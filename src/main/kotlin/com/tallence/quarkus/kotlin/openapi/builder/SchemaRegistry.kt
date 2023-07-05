package com.tallence.quarkus.kotlin.openapi.builder

import com.tallence.quarkus.kotlin.openapi.Schema
import com.tallence.quarkus.kotlin.openapi.SchemaRef

class SchemaRegistry {

    private val references = mutableMapOf<String, SchemaRef>()
    private val schemas = mutableMapOf<String, Schema>()

    val resolvedSchemas:Set<Schema>
        get() = schemas.values.toSet()

    fun getOrRegisterReference(ref: String) = references.getOrPut(ref) { SchemaRef(ref) }

    fun getOrRegisterType(type: String) = references.getOrPut(type) {
        schemas[type] = Schema.BasicTypeSchema(type)
        SchemaRef(type)
    }

    fun resolveRef(ref: String, schema: Schema) {
        schemas[ref] = schema
    }

    fun resolve(ref: SchemaRef) =
        schemas[ref.id] ?: throw IllegalArgumentException("Schema not found: ${ref.id}")

    fun unresolved() = references.filter { !schemas.containsKey(it.key) }.values

}