package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.util.*

class SchemaBuilder(
    private val schema: OpenApiSchema,
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
            addMapComponent(components)
            addAllOfComponent(components)
            addAnyOfComponent(components)
            addOneOfComponent(components)
            addModelNameComponent(components)
            addContainerModelNameComponent(components)
            addEnumItemNamesComponent(components)
            addValidationComponent(components)
            addObjectValidationComponent(components)
            addEnumValidationComponent(components)
        }

        schema.components = components
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
        }?.map(SchemaType::fromString)

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

    private fun ParseContext.addValidationComponent(components: MutableList<SchemaComponent>) {
        val validations = mutableListOf<SchemaValidation>()

        addCustomConstraintsValidation(validations)
        addArrayValidation(validations)
        addStringValidation(validations)
        addNumberValidation(validations)
        addPropertiesValidation(validations)

        if (validations.isNotEmpty()) {
            components.add(ValidationComponent(validations))
        }
    }

    private fun addCustomConstraintsValidation(validations: MutableList<SchemaValidation>) {
        node.getMultiValue("x-constraints")?.map { it.asText() }
            ?.let { validations.add(CustomConstraintsValidation(it)) }
    }

    private fun ParseContext.addArrayComponent(components: MutableList<SchemaComponent>) {
        node.get("items")?.let {
            val schema = contextFor(it, "items").parseAsSchema()
            components.add(ArrayItemsComponent(schema))
        }
    }

    private fun addArrayValidation(validations: MutableList<SchemaValidation>) {
        val minItems = node.getTextOrNull("minItems")?.toInt()
        val maxItems = node.getTextOrNull("maxItems")?.toInt()
        if (minItems != null || maxItems != null) {
            validations.add(ArrayValidation(minItems, maxItems))
        }
    }

    private fun addEnumValidationComponent(components: MutableList<SchemaComponent>) {
        val enumValues = node.withArray("enum").map { it.asText() }
        if (enumValues.isNotEmpty()) {
            components.add(EnumValidationComponent(enumValues))
        }
    }

    private fun addStringValidation(validations: MutableList<SchemaValidation>) {
        val minLength = node.getTextOrNull("minLength")?.toInt()
        val maxLength = node.getTextOrNull("maxLength")?.toInt()
        val pattern = node.getTextOrNull("pattern")
        if (minLength != null || maxLength != null || pattern != null) {
            validations.add(StringValidation(minLength, maxLength, pattern))
        }
    }

    private fun ParseContext.addNumberValidation(validations: MutableList<SchemaValidation>) {
        val minimum = extractComparableNumber("minimum")
        val maximum = extractComparableNumber("maximum")
        if (minimum != null || maximum != null) {
            validations.add(NumberValidation(minimum, maximum))
        }
    }

    private fun addPropertiesValidation(validations: MutableList<SchemaValidation>) {
        val minProperties = node.getTextOrNull("minProperties")?.toInt()
        val maxProperties = node.getTextOrNull("maxProperties")?.toInt()
        if (minProperties != null || maxProperties != null) {
            validations.add(PropertiesValidation(minProperties, maxProperties))
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
        val options = node.withArray("allOf")
            .mapIndexed { idx, it ->
                contextFor(it, "allOf", "$idx").parseAsSchema()
            }
            .map { SomeOfOption(it) }

        if (options.isNotEmpty()) {
            components.add(AllOfComponent(options))
        }
    }

    private fun ParseContext.addAnyOfComponent(components: MutableList<SchemaComponent>) {
        val options = node.withArray("anyOf")
            .mapIndexed { idx, it ->
                contextFor(it, "anyOf", "$idx").parseAsSchema()
            }
            .map { SomeOfOption(it) }

        if (options.isNotEmpty()) {
            components.add(AnyOfComponent(options))
        }
    }

    private fun ParseContext.addOneOfComponent(components: MutableList<SchemaComponent>) {
        val options = node.withArray("oneOf")
            .mapIndexed { idx, it ->
                contextFor(it, "oneOf", "$idx").parseAsSchema()
            }
            .map { SomeOfOption(it) }

        if (options.isNotEmpty()) {
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

            components.add(OneOfComponent(options, discriminator))
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

    private fun ParseContext.addMapComponent(components: MutableList<SchemaComponent>) {
        val additionalPropertiesNode = node.get("additionalProperties") ?: return

        // value is a boolean and set to false. break here
        if (additionalPropertiesNode is BooleanNode && !additionalPropertiesNode.booleanValue()) {
            return
        }

        // value is a string and set to "false". break here
        if (additionalPropertiesNode is TextNode && additionalPropertiesNode.textValue() == "false") {
            return
        }

        val mapValueSchema = contextFor(additionalPropertiesNode, "additionalProperties")
            .also { contextNode.asObjectNode { "Only json object supported for $contextPath" } }
            .parseAsSchema()

        components.add(MapComponent(mapValueSchema))
    }

    private fun addObjectValidationComponent(components: MutableList<SchemaComponent>) {
        val required = node.withArray("required").map { it.asText() }
        if (required.isNotEmpty()) {
            components.add(ObjectValidationComponent(required))
        }
    }

    private fun addModelNameComponent(components: MutableList<SchemaComponent>) {
        node.getTextOrNull("x-model-name")?.let { components.add(ModelNameComponent(it)) }
    }

    private fun addContainerModelNameComponent(components: MutableList<SchemaComponent>) {
        node.getTextOrNull("x-container-model-name")?.let { components.add(ContainerModelNameComponent(it)) }
    }

    private fun addEnumItemNamesComponent(components: MutableList<SchemaComponent>) {
        val names = node.with("x-enum-item-names")
            .propertiesAsList()
            .associate { (enumName, modelNameNode) -> enumName to modelNameNode.asText() }

        if (names.isNotEmpty()) {
            components.add(EnumItemNamesComponent(names))
        }
    }

}

fun ParseContext.parseAsSchema() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let { schemaCollector.registerSchema(contextPath) }

fun ParseContext.parseAsSchemaInto(schema: OpenApiSchema) {
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaBuilder(schema, it).run { this@parseAsSchemaInto.build() }
        }
}
