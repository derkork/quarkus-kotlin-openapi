package com.tallence.quarkus.kotlin.openapi.builder

import com.tallence.quarkus.kotlin.openapi.Schema
import com.tallence.quarkus.kotlin.openapi.SchemaRef

class SchemaRegistry {

    private val references = mutableMapOf<String, SchemaRef>()
    private val schemas = mutableMapOf<String, Schema>()

    fun getOrRegisterReference(ref: String) = references.getOrPut(ref) { SchemaRef(ref) }

    fun getOrRegisterType(type: String) = references.getOrPut(type) {
        schemas[type] = Schema.BasicTypeSchema(type)
        SchemaRef(type)
    }

    fun resolveRef(ref: String, schema: Schema.ComplexSchema) {
        schemas[ref] = schema
    }

    fun validate() {
        check(schemas.keys.containsAll(references.keys)) { "One or more referenced schemas missing" }
    }
}