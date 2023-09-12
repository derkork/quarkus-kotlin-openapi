package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class SchemaBuilder(
    private val typeName: String,
    private val node: ObjectNode,
    private val schemaRegistry: SchemaRegistry
) {

    fun build(): SchemaRef {
        // it's a real object with properties
        return when {
            node.has("properties") -> buildObjectTypeSchema()
            node.has("enum") -> buildEnumSchema()
            node.has("oneOf") -> buildOneOfSchema()
            node.has("allOf") -> buildAllOfSchema()
            node.has("anyOf") -> buildAnyOfSchema()
            node.getTextOrNull("type") == "array" -> buildArraySchema()
            else -> return buildPrimitiveTypeSchema()
        }
    }

    private fun buildObjectTypeSchema(): SchemaRef {
        val required = node.withArray("required").map { it.asText() }
        val properties = node.with("properties")
            .fields().asSequence()
            .map { (propertyName, propertyNode) ->
                propertyNode.parseAsSchemaProperty(
                    propertyName,
                    schemaRegistry,
                    required
                ) { "$typeName $propertyName" }
            }
            .toList()
        val ref = schemaRegistry.getOrRegisterType(typeName)
        schemaRegistry.resolveRef(typeName, Schema.ObjectTypeSchema(typeName, properties))
        return ref
    }

    private fun buildEnumSchema(): SchemaRef {
        val values = node.withArray("enum").map { it.asText() }
        val ref = schemaRegistry.getOrRegisterType(typeName)
        schemaRegistry.resolveRef(typeName, Schema.EnumSchema(typeName, values))
        return ref
    }

    private fun buildOneOfSchema(): SchemaRef {
        val schemas = node.withArray("oneOf")
            .filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it -> it.extractSchemaRef(schemaRegistry) { "$typeName OneOf $idx" } }
        val ref = schemaRegistry.getOrRegisterType(typeName)
        val discriminator = node.resolvePath("discriminator/propertyName")?.asText()
            ?: throw IllegalStateException("discriminator is required for oneOf schemas")
        schemaRegistry.resolveRef(typeName, Schema.OneOfSchema(typeName, discriminator, schemas))
        return ref
    }

    private fun buildAllOfSchema(): SchemaRef {
        val schemas = node.withArray("allOf")
            .filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it -> it.extractSchemaRef(schemaRegistry) { "$typeName AllOf $idx" } }
        val ref = schemaRegistry.getOrRegisterType(typeName)
        schemaRegistry.resolveRef(typeName, Schema.AllOfSchema(typeName, schemas))
        return ref
    }

    private fun buildAnyOfSchema(): SchemaRef {
        val schemas = node.withArray("anyOf")
            .filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it -> it.extractSchemaRef(schemaRegistry) { "$typeName AnyOf $idx" } }
        val ref = schemaRegistry.getOrRegisterType(typeName)
        schemaRegistry.resolveRef(typeName, Schema.AnyOfSchema(typeName, schemas))
        return ref
    }

    private fun buildArraySchema(): SchemaRef {
        val items = node.with("items").extractSchemaRef(schemaRegistry) { "$typeName items" }
        val ref = schemaRegistry.getOrRegisterType(typeName)
        schemaRegistry.resolveRef(typeName, Schema.ArraySchema(items))
        return ref
    }

    private fun buildPrimitiveTypeSchema(): SchemaRef {
        val additionalProperties = node.get("additionalProperties")
        var type = node.getTextOrNull("type") ?: "string"
        if (type == "object") {
            check(additionalProperties != null) { "additionalProperties is required for object types" }
            type = additionalProperties.getTextOrNull("type") ?: "string"
        }

        if (additionalProperties != null) {
            val format = additionalProperties.getTextOrNull("format")
            if (format != null) {
                type = format
            }
        }

        return schemaRegistry.getOrRegisterType(type)
    }
}

fun JsonNode.parseAsSchema(typeName: String, schemaRegistry: SchemaRegistry): SchemaRef {
    require(this.isObject) { "Json object expected" }

    return SchemaBuilder(typeName, this as ObjectNode, schemaRegistry).build()
}

fun ObjectNode.extractSchemaRef(schemaRegistry: SchemaRegistry, typeNameHint: () -> String): SchemaRef {
    // reference to another schema
    val ref = this["\$ref"]?.asText()
    if (ref != null) {
        return schemaRegistry.getOrRegisterReference(ref)
    }
    // an inline schema
    return this.parseAsSchema(typeNameHint(), schemaRegistry)
}

