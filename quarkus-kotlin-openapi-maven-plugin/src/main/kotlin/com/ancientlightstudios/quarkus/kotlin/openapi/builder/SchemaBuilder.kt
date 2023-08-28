package com.ancientlightstudios.quarkus.kotlin.openapi.builder

import com.ancientlightstudios.quarkus.kotlin.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.SchemaRef
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ancientlightstudios.quarkus.kotlin.openapi.getTextOrNull
import com.ancientlightstudios.quarkus.kotlin.openapi.resolvePath
import java.util.*

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
            else -> return buildPrimitiveTypeSchema()
        }
    }

    private fun buildObjectTypeSchema(): SchemaRef {
        val properties = node.with("properties")
            .fields().asSequence()
            .map { (propertyName, propertyNode) -> propertyNode.parseAsSchemaProperty(propertyName, schemaRegistry)  { "$typeName $propertyName" } }
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
            .mapIndexed{ idx, it -> it.extractSchemaRef(schemaRegistry) { "$typeName OneOf $idx" } }
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
            .mapIndexed { idx, it ->  it.extractSchemaRef(schemaRegistry) { "$typeName AnyOf $idx" } }
        val ref = schemaRegistry.getOrRegisterType(typeName)
        schemaRegistry.resolveRef(typeName, Schema.AnyOfSchema(typeName, schemas))
        return ref
    }


    private fun buildPrimitiveTypeSchema(): SchemaRef {
        val type = node.getTextOrNull("type") ?: "string"
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

