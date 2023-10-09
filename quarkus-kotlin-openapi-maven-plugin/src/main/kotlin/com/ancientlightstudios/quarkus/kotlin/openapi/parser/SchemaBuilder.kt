package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.*
import com.fasterxml.jackson.databind.node.ObjectNode

class SchemaBuilder(private val node: ObjectNode) {

    fun ParseContext.build(): Schema {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractSchemaDefinition()
            else -> extractSchemaReference(ref)
        }
    }

    private fun ParseContext.extractSchemaDefinition(): Schema {
        val type = extractPrimaryType()
        return when {
            type == "object" || type == null && node.has("properties") -> extractObjectSchemaDefinition()
            type == null -> throw IllegalStateException("Unsupported schema type in $contextPath")
            node.has("enum") -> extractEnumSchemaDefinition(type)
            node.has("oneOf") -> extractOneOfSchemaDefinition()
            node.has("allOf") -> extractAllOfSchemaDefinition()
            node.has("anyOf") -> extractAnyOfSchemaDefinition()
            type == "array" -> extractArraySchemaDefinition()
            else -> extractPrimitiveSchemaDefinition(type)
        }
    }

    private fun ParseContext.extractPrimaryType() = when (openApiVersion) {
        OpenApiVersion.V3_0 -> node.getTextOrNull("type")
        OpenApiVersion.V3_1 -> node.withArray("type")
            .map { it.asText() }
            .filterNot { it == "null" }
            .also { check(it.size <= 1) { "Schema with multiple types (beside null) not yet supported. $contextPath" } }
            .firstOrNull()
    }

    private fun ParseContext.isNullable() = when (openApiVersion) {
        OpenApiVersion.V3_0 -> node.getBooleanOrNull("nullable") ?: false
        OpenApiVersion.V3_1 -> node.withArray("type")
            .map { it.asText() }
            .contains("null")
    }

    private fun ParseContext.extractSchemaReference(ref: String): Schema {
        val (targetName, schema) = referenceResolver.resolveSchema(ref)
        val description = when (openApiVersion) {
            // not supported in v3.0
            OpenApiVersion.V3_0 -> null
            OpenApiVersion.V3_1 -> node.getTextOrNull("description")
        }

        // extract into functions if other versions support more overrides for parameter references
        return when (schema) {
            is Schema.PrimitiveSchema -> PrimitiveSchemaReference(targetName, schema, description)
            is Schema.EnumSchema -> EnumSchemaReference(targetName, schema, description)
            is Schema.ArraySchema -> ArraySchemaReference(targetName, schema, description)
            is Schema.ObjectSchema -> ObjectSchemaReference(targetName, schema, description)
            is Schema.OneOfSchema -> OneOfSchemaReference(targetName, schema, description)
            is Schema.AllOfSchema -> AllOfSchemaReference(targetName, schema, description)
            is Schema.AnyOfSchema -> AnyOfSchemaReference(targetName, schema, description)
        }
    }

    private fun ParseContext.extractPrimitiveSchemaDefinition(type: String): PrimitiveSchemaDefinition {
        return PrimitiveSchemaDefinition(
            node.getTextOrNull("description"), isNullable(), type,
            node.getTextOrNull("format"), node.getTextOrNull("default")
        )
    }

    private fun ParseContext.extractEnumSchemaDefinition(type: String): EnumSchemaDefinition {
        val values = node.withArray("enum").map { it.asText() }
        val defaultValue = node.get("default")?.asText()
        if (defaultValue != null) {
            check(values.contains(defaultValue)) { "Default value '$defaultValue' is not a valid enum value for $contextPath" }
        }

        return EnumSchemaDefinition(
            node.getTextOrNull("description"), isNullable(), type,
            node.getTextOrNull("format"), values, defaultValue
        )
    }

    private fun ParseContext.extractArraySchemaDefinition(): ArraySchemaDefinition {
        return ArraySchemaDefinition(
            node.getTextOrNull("description"),
            isNullable(), contextFor("items").parseAsSchema()
        )
    }

    private fun ParseContext.extractObjectSchemaDefinition(): ObjectSchemaDefinition {
        val required = node.withArray("required").map { it.asText() }
        val properties = node.with("properties")
            .propertiesAsList()
            .map { (name, propertyNode) ->
                val property = contextFor(propertyNode, "properties.$name")
                    .parseAsSchemaProperty(required.contains(name))
                name to property
            }

        return ObjectSchemaDefinition(node.getTextOrNull("description"), isNullable(), properties)
    }

    private fun ParseContext.extractOneOfSchemaDefinition(): OneOfSchemaDefinition {
        val types = node.withArray("oneOf")
        check(types.all { it is ObjectNode }) { "OneOf schema with other types than objects not yet supported. $contextPath" }
        val schemas = types.filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it ->
                contextFor(it, "oneOf[$idx]").parseAsSchema()
            }

        return OneOfSchemaDefinition(
            node.getTextOrNull("description"), isNullable(),
            schemas, node.getTextOrNull("discriminator")
        )
    }

    private fun ParseContext.extractAllOfSchemaDefinition(): AllOfSchemaDefinition {
        val types = node.withArray("allOf")
        check(types.all { it is ObjectNode }) { "AllOf schema with other types than objects not yet supported. $contextPath" }
        val schemas = types.filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it ->
                contextFor(it, "allOf[$idx]").parseAsSchema()
            }

        return AllOfSchemaDefinition(node.getTextOrNull("description"), isNullable(), schemas)
    }

    private fun ParseContext.extractAnyOfSchemaDefinition(): AnyOfSchemaDefinition {
        val types = node.withArray("anyOf")
        check(types.all { it is ObjectNode }) { "AnyOf schema with other types than objects not yet supported. $contextPath" }
        val schemas = types.filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it ->
                contextFor(it, "anyOf[$idx]").parseAsSchema()
            }

        return AnyOfSchemaDefinition(node.getTextOrNull("description"), isNullable(), schemas)
    }

}

fun ParseContext.parseAsSchema() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaBuilder(it).run { this@parseAsSchema.build() }
        }
