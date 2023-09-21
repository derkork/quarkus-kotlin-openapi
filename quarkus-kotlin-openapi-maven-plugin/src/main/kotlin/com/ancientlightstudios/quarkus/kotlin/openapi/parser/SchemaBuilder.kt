package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

class SchemaBuilder(
    private val typeName: String,
    private val shared: Boolean,  // TODO: should we use this flag for other schemas like arrays too?
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
        return schemaRegistry.getOrRegisterReference(typeName).also {
            schemaRegistry.resolveRef(it, Schema.ObjectTypeSchema(typeName, properties))
        }
    }

    private fun buildEnumSchema(): SchemaRef {
        val values = node.withArray("enum").map { it.asText() }
        return schemaRegistry.getOrRegisterReference(typeName).also {
            schemaRegistry.resolveRef(it, Schema.EnumSchema(typeName, values))
        }
    }

    private fun buildOneOfSchema(): SchemaRef {
        val schemas = node.withArray("oneOf")
            .filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it -> it.extractSchemaRef(schemaRegistry) { "$typeName OneOf $idx" } }
        val discriminator = node.resolvePath("discriminator/propertyName")?.asText()
            ?: throw IllegalStateException("discriminator is required for oneOf schemas")
        return schemaRegistry.getOrRegisterReference(typeName).also {
            schemaRegistry.resolveRef(it, Schema.OneOfSchema(typeName, discriminator, schemas))
        }
    }

    private fun buildAllOfSchema(): SchemaRef {
        val schemas = node.withArray("allOf")
            .filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it -> it.extractSchemaRef(schemaRegistry) { "$typeName AllOf $idx" } }
        return schemaRegistry.getOrRegisterReference(typeName).also {
            schemaRegistry.resolveRef(it, Schema.AllOfSchema(typeName, schemas))
        }
    }

    private fun buildAnyOfSchema(): SchemaRef {
        val schemas = node.withArray("anyOf")
            .filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it -> it.extractSchemaRef(schemaRegistry) { "$typeName AnyOf $idx" } }
        return schemaRegistry.getOrRegisterReference(typeName).also {
            schemaRegistry.resolveRef(it, Schema.AnyOfSchema(typeName, schemas))
        }
    }

    private fun buildArraySchema(): SchemaRef {
        val items = node.with("items").extractSchemaRef(schemaRegistry) { "$typeName items" }
        return schemaRegistry.getOrRegisterReference(typeName).also {
            schemaRegistry.resolveRef(it, Schema.ArraySchema(items))
        }
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
        return schemaRegistry.getOrRegisterReference(typeName).also {
            schemaRegistry.resolveRef(it, Schema.PrimitiveTypeSchema(typeName, type, shared))
        }
    }
}

fun JsonNode.parseAsSchema(typeName: String, schemaRegistry: SchemaRegistry, shared: Boolean = true): SchemaRef {
    require(this.isObject) { "Json object expected" }

    return SchemaBuilder(typeName, shared, this as ObjectNode, schemaRegistry).build()
}

fun ObjectNode.extractSchemaRef(schemaRegistry: SchemaRegistry, typeNameHint: () -> String): SchemaRef {
    // reference to another schema
    this["\$ref"]?.let {
        return schemaRegistry.getOrRegisterReference(it.asText())
    }

    // an inline schema
    return this.parseAsSchema(typeNameHint(), schemaRegistry, false)
}

