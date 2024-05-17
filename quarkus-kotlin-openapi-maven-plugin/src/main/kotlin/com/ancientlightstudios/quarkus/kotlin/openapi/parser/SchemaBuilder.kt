package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.DefaultSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*

class SchemaBuilder(
    private val schema: TransformableSchema,
    private val node: ObjectNode
) {

    fun ParseContext.build() {
        // schema references can be paired with all other schema components since version 3.1. This is not allowed
        // for other references (e.g. parameter or request bodies), so this code works a little bit different.
        val components = mutableListOf<SchemaComponent>()

        val baseDefinitionAvailable = addBaseDefinitionComponent(components)
        if (!baseDefinitionAvailable || openApiVersion != ApiVersion.V3_0) {
            // if no $ref was found or version is at least 3.1.x we can add other components too
            addDirectionComponent(components)
            addDefaultComponent(components)
            addTypeComponent(components)
            addFormatComponent(components)
            addNullableComponent(components)
            addArrayComponent(components)
            addObjectComponent(components)
            addAllOfComponent(components)
            addAnyOfComponent(components)
            addOneOfComponent(components)
            addCustomConstraintsValidationComponent(components)
            addArrayValidationComponent(components)
            addObjectValidationComponent(components)
            addEnumValidationComponent(components)
            addStringValidationComponent(components)
            addNumberValidationComponent(components)
        }

        schema.components = components
        schema.name = contextPath.nameSuggestion() ?: ""
    }

    private fun ParseContext.addBaseDefinitionComponent(components: MutableList<SchemaComponent>): Boolean {
        return node.getTextOrNull("\$ref")?.let {
            val schema = schemaCollector.registerSchema(it)
            components.add(BaseSchemaComponent(schema))
            true
        } ?: false
    }

    private fun ParseContext.addTypeComponent(components: MutableList<SchemaComponent>) {
        val types = when (openApiVersion) {
            ApiVersion.V3_0 -> node.getTextOrNull("type")?.let { listOf(it) }
            ApiVersion.V3_1 -> node.getMultiValue("type")?.map { it.asText() }?.filterNot { it == "null" }
        }?.map(SchemaTypes::fromString)

        if (types.isNullOrEmpty()) {
            return
        }

        if (types.size > 1) {
            SpecIssue("Schemas with more than one type is not supported. Found in $contextPath")
        }

        components.add(TypeComponent(types.first()))
    }

    private fun ParseContext.addFormatComponent(components: MutableList<SchemaComponent>) {
        val format = node.getTextOrNull("format")
        if (format != null) {
            components.add(FormatComponent(format))
        }
    }

    private fun ParseContext.addNullableComponent(components: MutableList<SchemaComponent>) {
        val nullable = when (openApiVersion) {
            ApiVersion.V3_0 -> node.getBooleanOrNull("nullable")
            ApiVersion.V3_1 -> node.getMultiValue("type")?.any { it.asText() == "null" }
        }

        if (nullable != null) {
            components.add(NullableComponent(nullable))
        }
    }

    private fun ParseContext.addDirectionComponent(components: MutableList<SchemaComponent>) {
        val readOnly = node.getBooleanOrNull("readOnly") ?: false
        val writeOnly = node.getBooleanOrNull("writeOnly") ?: false

        val direction = when {
            readOnly && writeOnly -> SpecIssue("Property can't be read-only and write-only at the same time. $contextPath")
            readOnly -> SchemaModifier.ReadOnly
            writeOnly -> SchemaModifier.WriteOnly
            else -> null
        }

        direction?.let { components.add(SchemaModifierComponent(direction)) }
    }

    private fun addCustomConstraintsValidationComponent(components: MutableList<SchemaComponent>) {
        node.getMultiValue("x-constraints")?.map { it.asText() }
            ?.let { components.add(ValidationComponent(CustomConstraintsValidation(it))) }
    }

    private fun ParseContext.addArrayComponent(components: MutableList<SchemaComponent>) {
        node.get("items")?.let {
            val schema = contextFor(it, "items").parseAsSchema()
            components.add(ArrayItemsComponent(schema))
        }
    }

    private fun addArrayValidationComponent(components: MutableList<SchemaComponent>) {
        val minItems = node.getTextOrNull("minItems")?.toInt()
        val maxItems = node.getTextOrNull("maxItems")?.toInt()
        if (minItems != null || maxItems != null) {
            components.add(ValidationComponent(ArrayValidation(minItems, maxItems)))
        }
    }

    private fun addEnumValidationComponent(components: MutableList<SchemaComponent>) {
        val enumValues = node.withArray("enum").map { it.asText() }
        if (enumValues.isNotEmpty()) {
            components.add(EnumValidationComponent(enumValues))
        }
    }

    private fun addStringValidationComponent(components: MutableList<SchemaComponent>) {
        val minLength = node.getTextOrNull("minLength")?.toInt()
        val maxLength = node.getTextOrNull("maxLength")?.toInt()
        val pattern = node.getTextOrNull("pattern")
        if (minLength != null || maxLength != null || pattern != null) {
            components.add(ValidationComponent(StringValidation(minLength, maxLength, pattern)))
        }
    }

    private fun ParseContext.addNumberValidationComponent(components: MutableList<SchemaComponent>) {
        val minimum = extractComparableNumber("minimum")
        val maximum = extractComparableNumber("maximum")
        if (minimum != null || maximum != null) {
            components.add(ValidationComponent(NumberValidation(minimum, maximum)))
        }
    }

    private fun ParseContext.extractComparableNumber(name: String): ComparableNumber? {
        val capitalizedPostfix = name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString()
        }

        return when (openApiVersion) {
            ApiVersion.V3_0 -> node.getTextOrNull(name)?.let {
                ComparableNumber(it, node.getBooleanOrNull("exclusive$capitalizedPostfix") ?: false)
            }

            ApiVersion.V3_1 -> node.getTextOrNull(name)?.let {
                ComparableNumber(it, false)
            } ?: node.getTextOrNull("exclusive$capitalizedPostfix")?.let {
                ComparableNumber(it, true)
            }
        }
    }

    private fun addDefaultComponent(components: MutableList<SchemaComponent>) {
        node.getTextOrNull("default")?.let { components.add(DefaultComponent(it)) }
    }

    private fun ParseContext.addAllOfComponent(components: MutableList<SchemaComponent>) {
        val schemas = node.withArray("allOf")
            .mapIndexed { idx, it ->
                contextFor(it, "allOf", "$idx").parseAsSchema()
            }.map { DefaultSchemaUsage(it) }

        if (schemas.isNotEmpty()) {
            components.add(AllOfComponent(schemas))
        }
    }

    private fun ParseContext.addAnyOfComponent(components: MutableList<SchemaComponent>) {
        val schemas = node.withArray("anyOf")
            .mapIndexed { idx, it ->
                contextFor(it, "anyOf", "$idx").parseAsSchema()
            }.map { DefaultSchemaUsage(it) }

        if (schemas.isNotEmpty()) {
            components.add(AnyOfComponent(schemas))
        }
    }

    private fun ParseContext.addOneOfComponent(components: MutableList<SchemaComponent>) {
        val schemas = node.withArray("oneOf")
            .mapIndexed { idx, it ->
                contextFor(it, "oneOf", "$idx").parseAsSchema()
            }.map { DefaultSchemaUsage(it) }

        if (schemas.isNotEmpty()) {
            val discriminatorNode = node.get("discriminator")
                ?.asObjectNode { "Json object expected for discriminator at $contextPath" }
            var discriminator: OneOfDiscriminator? = null
            if (discriminatorNode != null) {
                val propertyName = discriminatorNode.get("propertyName")?.asText()
                    ?: SpecIssue("Discriminator without propertyName found")

                val mappings = discriminatorNode.with("mapping")
                    .propertiesAsList()
                    .toMap()
                    .mapValues { it.value.asText() }

                discriminator = OneOfDiscriminator(propertyName, mappings)
            }

            components.add(OneOfComponent(schemas, discriminator))
        }
    }

    private fun ParseContext.addObjectComponent(components: MutableList<SchemaComponent>) {
        val properties = node.with("properties")
            .propertiesAsList()
            .map { (name, propertyNode) ->
                contextFor(propertyNode, "properties", name).parseAsSchemaProperty(name)
            }

        if (properties.isNotEmpty()) {
            components.add(ObjectComponent(properties))
        }
    }

    private fun addObjectValidationComponent(components: MutableList<SchemaComponent>) {
        val required = node.withArray("required").map { it.asText() }
        if (required.isNotEmpty()) {
            components.add(ObjectValidationComponent(required))
        }
    }

//    private fun ParseContext.extractOneOfValidation(target: MutableList<OpenApiValidation>) {
//        val fragments = node.withArray("oneOf")
//            .mapIndexed { idx, it ->
//                contextFor(it, "oneOf[$idx]").parseAsSchema()
//            }
//
//        val discriminator = node.resolvePointer(JsonPointer.fromSegments("discriminator", "propertyName"))?.asText()
//
//        // process mapping overrides
//        val additionalMappings = node.resolvePointer(JsonPointer.fromSegments("discriminator", "mapping"))
//            ?.asObjectNode { "Discriminator mapping must be an object. $contextPath" }
//            ?.propertiesAsList()
//            ?.map { (key, node) ->
//                val reference = node.asText()
//                val index = fragments.indexOfFirst { it is OpenApiSchema.Reference && it.targetPath == reference }
//                if (index < 0) {
//                    SpecIssue("Can't find schema referenced by discriminator mapping $reference in $contextPath")
//                }
//                key to index
//            } ?: listOf()
//
//        if (fragments.isNotEmpty() || discriminator != null || additionalMappings.isNotEmpty()) {
//            target.add(
//                OpenApiOneOfValidation(
//                    fragments.mapTo(mutableListOf()) {
//                        OpenApiSchemaUsage(OpenApiMetadata(), it)
//                    },
//                    discriminator,
//                    additionalMappings.toMutableList()
//                )
//            )
//        }
//    }
//
}

fun ParseContext.parseAsSchema() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let { schemaCollector.registerSchema(contextPath) }

fun ParseContext.parseAsSchemaInto(schema: TransformableSchema) {
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaBuilder(schema, it).run { this@parseAsSchemaInto.build() }
        }
}
