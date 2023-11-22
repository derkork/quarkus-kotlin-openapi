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
            type == "object" -> extractObjectSchemaDefinition()
            type == "array" -> extractArraySchemaDefinition()
            else -> extractPrimitiveSchemaDefinition(type)
        }
    }

    private fun ParseContext.extractPrimaryType() = when (openApiVersion) {
        OpenApiVersion.V3_0 -> node.getTextOrNull("type")
        OpenApiVersion.V3_1 -> node.getMultiValue("type")
            ?.map { it.asText() }
            ?.filterNot { it == "null" }
            ?.also { check(it.size <= 1) { "Schema with multiple types (beside null) not yet supported. $contextPath" } }
            ?.firstOrNull()
    }

    private fun ParseContext.isNullable() = when (openApiVersion) {
        OpenApiVersion.V3_0 -> node.getBooleanOrNull("nullable") ?: false
        OpenApiVersion.V3_1 -> node.getMultiValue("type")
            ?.map { it.asText() }
            ?.contains("null") ?: false
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
            is Schema.PrimitiveSchema -> PrimitiveSchemaReference(contextPath, targetName, schema, description)
            is Schema.ArraySchema -> ArraySchemaReference(contextPath, targetName, schema, description)
            is Schema.ObjectSchema -> ObjectSchemaReference(contextPath, targetName, schema, description)
            is Schema.OneOfSchema -> OneOfSchemaReference(contextPath, targetName, schema, description)
            is Schema.AllOfSchema -> AllOfSchemaReference(contextPath, targetName, schema, description)
            is Schema.AnyOfSchema -> AnyOfSchemaReference(contextPath, targetName, schema, description)
        }
    }

    private fun ParseContext.extractPrimitiveSchemaDefinition(type: String): PrimitiveSchemaDefinition {
        return PrimitiveSchemaDefinition(
            contextPath,
            node.getTextOrNull("description"), isNullable(), type,
            node.getTextOrNull("format"), node.getTextOrNull("default"),
            extractPrimitiveTypeValidation(type)
        )
    }

    private fun ParseContext.extractArraySchemaDefinition(): ArraySchemaDefinition {
        return ArraySchemaDefinition(
            contextPath,
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
            contextPath,
            node.getTextOrNull("description"),
            isNullable(), properties, extractDefaultValidation()
        )
    }

    private fun ParseContext.extractOneOfSchemaDefinition(): OneOfSchemaDefinition {
        val types = node.withArray("oneOf")
        val discriminator = node.resolvePointer("discriminator/propertyName")?.asText()
        check(types.all { it is ObjectNode }) { "OneOf schema with primitive types not yet supported. $contextPath" }
        val schemas = types.filterIsInstance<ObjectNode>()
            .mapIndexed { idx, it ->
                contextFor(it, "oneOf[$idx]").parseAsSchema()
            }


        val mappings = linkedMapOf<Schema, List<String>>()
        if (discriminator != null) {
            schemas.forEach {
                check(it is SchemaReference<*>) { "OneOf schema with discriminator does not support inline schemas. $contextPath" }
                mappings[it] = listOf(it.targetName)
            }
        } else {
            schemas.forEach { mappings[it] = emptyList() }
        }

        // process mapping overrides
        node.resolvePointer("discriminator/mapping")
            ?.asObjectNode { "Discriminator mapping must be an object. $contextPath" }
            ?.propertiesAsList()
            ?.associate { (name, node) -> name to referenceResolver.resolveSchema(node.asText()).first }
            ?.forEach {
                val key =
                    schemas.firstOrNull { schema -> schema is SchemaReference<*> && schema.targetName == it.value }
                        ?: throw IllegalStateException("Discriminator mapping must reference one of the oneOf schemas. $contextPath")

                mappings.compute(key) { _, value ->
                    value?.plus(it.key) ?: listOf(it.key)
                }
            }

        return OneOfSchemaDefinition(
            contextPath,
            node.getTextOrNull("description"), isNullable(),
            mappings, discriminator,
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
            contextPath,
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
            contextPath,
            node.getTextOrNull("description"),
            isNullable(), schemas, extractDefaultValidation()
        )
    }

    private fun ParseContext.extractPrimitiveTypeValidation(type: String): List<Validation> {
        val result = mutableListOf<Validation>()
        when (type) {
            "string" -> {
                val minLength = node.getTextOrNull("minLength")?.toInt()
                val maxLength = node.getTextOrNull("maxLength")?.toInt()
                val pattern = node.getTextOrNull("pattern")
                if (minLength != null || maxLength != null || pattern != null) {
                    result.add(StringValidation(minLength, maxLength, pattern))
                }
            }

            "number", "integer" -> {
                val minimum = extractComparableNumber("minimum")
                val maximum = extractComparableNumber("maximum")
                if (minimum != null || maximum != null) {
                    result.add(NumberValidation(minimum, maximum))
                }
            }
        }

        val enumValues = node.withArray("enum").map { it.asText() }
        if (enumValues.isNotEmpty()) {
            result.add(EnumValidation(enumValues))
        }

        extractCustomValidationRules()?.let { result.add(it) }
        return result
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

    private fun extractArrayValidation(): List<Validation> {
        val result = mutableListOf<Validation>()

        val minItems = node.getTextOrNull("minItems")?.toInt()
        val maxItems = node.getTextOrNull("maxItems")?.toInt()
        if (minItems != null || maxItems != null) {
            result.add(ArrayValidation(minItems, maxItems))
        }

        extractCustomValidationRules()?.let { result.add(it) }
        return result
    }


    private fun extractDefaultValidation(): List<Validation> {
        val result = mutableListOf<Validation>()
        extractCustomValidationRules()?.let { result.add(it) }
        return result
    }

    private fun extractCustomValidationRules(): CustomConstraintsValidation? {
        val list = when (val customConstraints = node["x-constraints"]) {
            is TextNode -> listOf(customConstraints.asText())
            is ArrayNode -> customConstraints.map { it.asText() }
            else -> return null
        }
        return CustomConstraintsValidation(list)
    }

}

fun ParseContext.parseAsSchema() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaBuilder(it).run { this@parseAsSchema.build() }
        }
