package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.*
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.util.*

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
            node.has("oneOf") -> extractOneOfSchemaDefinition()
            node.has("allOf") -> extractAllOfSchemaDefinition()
            node.has("anyOf") -> extractAnyOfSchemaDefinition()
            type == null && node.has("properties") -> extractObjectSchemaDefinition()
            type == null -> throw IllegalStateException("Unsupported schema type in $contextPath")
            node.has("enum") -> extractEnumSchemaDefinition(type)
            type == "object" -> extractObjectSchemaDefinition()
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
            node.getTextOrNull("format"), node.getTextOrNull("default"),
            extractPrimitiveTypeValidation(type)
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
            node.getTextOrNull("format"), values, defaultValue,
            extractPrimitiveTypeValidation(type)
        )
    }

    private fun ParseContext.extractArraySchemaDefinition(): ArraySchemaDefinition {
        return ArraySchemaDefinition(
            node.getTextOrNull("description"),
            isNullable(), contextFor("items").parseAsSchema(),
            extractArrayValidation()
        )
    }

    private fun ParseContext.extractObjectSchemaDefinition(): ObjectSchemaDefinition {
        val required = node.withArray("required").map { it.asText() }
        val properties = node.with("properties")
            .propertiesAsList()
            .map { (name, propertyNode) ->
                val property = contextFor(propertyNode, "properties/$name")
                    .parseAsSchemaProperty(required.contains(name))
                name to property
            }

        return ObjectSchemaDefinition(
            node.getTextOrNull("description"),
            isNullable(), properties, extractDefaultValidation()
        )
    }

    private fun ParseContext.extractOneOfSchemaDefinition(): OneOfSchemaDefinition {
        val types = node.withArray("oneOf")
        check(types.all { it is ObjectNode }) { "OneOf schema with other types than objects not yet supported. $contextPath" }
        val schemas = types.filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it ->
                contextFor(it, "oneOf[$idx]").parseAsSchema()
            }
            .map {
                check(it is Schema.ObjectSchema) { "OneOf schema only supports object schemas. $contextPath" }
                it
            }

        return OneOfSchemaDefinition(
            node.getTextOrNull("description"), isNullable(),
            schemas, node.getTextOrNull("discriminator"),
            extractDefaultValidation()
        )
    }

    private fun ParseContext.extractAllOfSchemaDefinition(): AllOfSchemaDefinition {
        val types = node.withArray("allOf")
        check(types.all { it is ObjectNode }) { "AllOf schema with other types than objects not yet supported. $contextPath" }
        val schemas = types.filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it ->
                contextFor(it, "allOf[$idx]").parseAsSchema()
            }
            .map {
                check(it is Schema.ObjectSchema) { "AllOf schema only supports object schemas. $contextPath" }
                it
            }

        return AllOfSchemaDefinition(
            node.getTextOrNull("description"), isNullable(),
            schemas, extractDefaultValidation()
        )
    }

    private fun ParseContext.extractAnyOfSchemaDefinition(): AnyOfSchemaDefinition {
        val types = node.withArray("anyOf")
        check(types.all { it is ObjectNode }) { "AnyOf schema with other types than objects not yet supported. $contextPath" }
        val schemas = types.filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it ->
                contextFor(it, "anyOf[$idx]").parseAsSchema()
            }
            .map {
                check(it is Schema.ObjectSchema) { "AnyOf schema only supports object schemas. $contextPath" }
                it
            }

        return AnyOfSchemaDefinition(
            node.getTextOrNull("description"),
            isNullable(), schemas, extractDefaultValidation()
        )
    }

    private fun ParseContext.extractPrimitiveTypeValidation(type: String) = when (type) {
        "string" -> StringValidation(
            node.getTextOrNull("minLength")?.toInt(),
            node.getTextOrNull("maxLength")?.toInt(),
            node.getTextOrNull("pattern"),
            extractCustomValidationRules()
        )

        "number", "integer" -> NumberValidation(
            extractComparableNumber("minimum"),
            extractComparableNumber("maximum"),
            extractCustomValidationRules()
        )

        else -> extractDefaultValidation()
    }

    private fun ParseContext.extractComparableNumber(name: String): ComparableNumber? {
        val capitalizedPostfix = name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
        }

        return when (openApiVersion) {
            OpenApiVersion.V3_0 -> node.getTextOrNull(name)?.let {
                ComparableNumber(it, node.getBooleanOrNull("exclusive$capitalizedPostfix") ?: false)
            }

            OpenApiVersion.V3_1 -> node.getTextOrNull(name)?.let {
                ComparableNumber(it, false)
            } ?: node.getTextOrNull("exclusive$capitalizedPostfix")?.let {
                ComparableNumber(it, true)
            }
        }
    }

    private fun extractArrayValidation() = ArrayValidation(
        node.getTextOrNull("minItems")?.toInt(),
        node.getTextOrNull("maxItems")?.toInt(),
        extractCustomValidationRules()
    )

    private fun extractDefaultValidation() = DefaultValidation(extractCustomValidationRules())

    private fun extractCustomValidationRules() = when (val customConstraints = node["x-constraints"]) {
        is TextNode -> listOf(customConstraints.asText())
        is ArrayNode -> customConstraints.map { it.asText() }
        else -> emptyList()
    }

}

fun ParseContext.parseAsSchema() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaBuilder(it).run { this@parseAsSchema.build() }
        }
