package com.tallence.quarkus.kotlin.openapi.builder

import com.tallence.quarkus.kotlin.openapi.Schema
import com.tallence.quarkus.kotlin.openapi.SchemaRef
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*

class SchemaBuilder(
    private val typeName: String,
    private val node: ObjectNode,
    private val schemaRegistry: SchemaRegistry
) {

    fun build(): Schema.ComplexSchema {
        val properties = node.with("properties")
            .fields().asSequence()
            .map { (propertyName, propertyNode) -> propertyNode.parseAsSchemaProperty(propertyName, schemaRegistry) }
            .toList()

        return Schema.ComplexSchema(typeName, properties)
    }
}

fun JsonNode.parseAsSchema(typeName: String, schemaRegistry: SchemaRegistry): Schema.ComplexSchema {
    if (!this.isObject) {
        throw IllegalArgumentException("Json object expected")
    }

    return SchemaBuilder(typeName, this as ObjectNode, schemaRegistry).build()
}

fun ObjectNode.extractSchemaRef(schemaRegistry: SchemaRegistry): SchemaRef {
    val ref = this["\$ref"]?.asText()
    if (ref != null) {
        return schemaRegistry.getOrRegisterReference(ref)
    }

    val type = this["type"]?.asText()
    when {
        type == "object" -> {
            val typeName = UUID.randomUUID().toString()
            val schemaRef = schemaRegistry.getOrRegisterReference(typeName)
            val schema = this.parseAsSchema(typeName, schemaRegistry)
            schemaRegistry.resolveRef(typeName, schema)
            return schemaRef
        }

        type != null -> return schemaRegistry.getOrRegisterType(type)
    }

    throw IllegalArgumentException("unknown schema definition")
}

