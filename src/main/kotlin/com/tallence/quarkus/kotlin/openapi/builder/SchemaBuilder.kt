package com.tallence.quarkus.kotlin.openapi.builder

import com.tallence.quarkus.kotlin.openapi.Schema
import com.tallence.quarkus.kotlin.openapi.SchemaRef
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.tallence.quarkus.kotlin.openapi.getTextOrNull
import java.util.*

class SchemaBuilder(
    private val typeName: String,
    private val node: ObjectNode,
    private val schemaRegistry: SchemaRegistry
) {

    fun build(): SchemaRef {
        // it's a real object with properties
        if (node.has("properties")) {
            return buildComplexSchema()
        }
        // it is some enum kind
        if (node.has("enum")) {
            return buildEnumSchema()
        }
        // it is a basic type
        return buildBasicTypeSchema()
    }

    private fun buildComplexSchema() : SchemaRef {
        val properties = node.with("properties")
            .fields().asSequence()
            .map { (propertyName, propertyNode) -> propertyNode.parseAsSchemaProperty(propertyName, schemaRegistry) }
            .toList()
        val ref = schemaRegistry.getOrRegisterType(typeName)
        schemaRegistry.resolveRef(typeName, Schema.ComplexSchema(typeName, properties))
        return ref
    }

    private fun buildEnumSchema() : SchemaRef {
        val values = node.withArray("enum").map { it.asText() }
        val ref = schemaRegistry.getOrRegisterType(typeName)
        schemaRegistry.resolveRef(typeName, Schema.EnumSchema(typeName, values))
        return ref
    }

    private fun buildBasicTypeSchema() : SchemaRef {
        val type = node.getTextOrNull("type") ?: "string"
        return schemaRegistry.getOrRegisterType(type)
    }
}

fun JsonNode.parseAsSchema(typeName: String, schemaRegistry: SchemaRegistry): SchemaRef {
    require(this.isObject) { "Json object expected" }

    return SchemaBuilder(typeName, this as ObjectNode, schemaRegistry).build()
}

fun ObjectNode.extractSchemaRef(schemaRegistry: SchemaRegistry): SchemaRef {
    // reference to another schema
    val ref = this["\$ref"]?.asText()
    if (ref != null) {
        return schemaRegistry.getOrRegisterReference(ref)
    }
    // an inline schema
    return this.parseAsSchema(UUID.randomUUID().toString(), schemaRegistry)
}

