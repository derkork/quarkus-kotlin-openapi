package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaModifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.SchemaTypes
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components.*
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*

class SchemaDefinitionBuilder(
    private val schemaDefinition: TransformableSchemaDefinition,
    private val node: ObjectNode
) {

    fun ParseContext.build() {
        // schema references can be paired with all other schema components since version 3.1. This is not allowed
        // for other references (e.g. parameter or request bodies), so this code works a little bit different.
        val components = mutableListOf<SchemaDefinitionComponent>()

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

        schemaDefinition.components = components
        schemaDefinition.name = contextPath.nameSuggestion() ?: ""
    }

    private fun ParseContext.addBaseDefinitionComponent(components: MutableList<SchemaDefinitionComponent>): Boolean {
        return node.getTextOrNull("\$ref")?.let {
            val schema = TransformableSchemaUsage(schemaDefinitionCollector.registerSchemaDefinition(it))
                .apply {
                    originPath = "$contextPath.\$ref"
                }
            components.add(BaseDefinitionComponent(schema))
            true
        } ?: false
    }

    private fun ParseContext.addTypeComponent(components: MutableList<SchemaDefinitionComponent>) {
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

    private fun ParseContext.addFormatComponent(components: MutableList<SchemaDefinitionComponent>) {
        val format = node.getTextOrNull("format")
        if (format != null) {
            components.add(FormatComponent(format))
        }
    }

    private fun ParseContext.addNullableComponent(components: MutableList<SchemaDefinitionComponent>) {
        val nullable = when (openApiVersion) {
            ApiVersion.V3_0 -> node.getBooleanOrNull("nullable")
            ApiVersion.V3_1 -> node.getMultiValue("type")?.any { it.asText() == "null" }
        }

        if (nullable != null) {
            components.add(NullableComponent(nullable))
        }
    }

    private fun ParseContext.addDirectionComponent(components: MutableList<SchemaDefinitionComponent>) {
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

    private fun addCustomConstraintsValidationComponent(components: MutableList<SchemaDefinitionComponent>) {
        node.getMultiValue("x-constraints")?.map { it.asText() }
            ?.let { components.add(CustomConstraintsValidationComponent(it)) }
    }

    private fun ParseContext.addArrayComponent(components: MutableList<SchemaDefinitionComponent>) {
        node.get("items")?.let {
            val schema = contextFor(it, "items").parseAsSchemaUsage()
            components.add(ArrayItemsComponent(schema))
        }
    }

    private fun addArrayValidationComponent(components: MutableList<SchemaDefinitionComponent>) {
        val minItems = node.getTextOrNull("minItems")?.toInt()
        val maxItems = node.getTextOrNull("maxItems")?.toInt()
        if (minItems != null || maxItems != null) {
            components.add(ArrayValidationComponent(minItems, maxItems))
        }
    }

    private fun addEnumValidationComponent(components: MutableList<SchemaDefinitionComponent>) {
        val enumValues = node.withArray("enum").map { it.asText() }
        if (enumValues.isNotEmpty()) {
            components.add(EnumValidationComponent(enumValues))
        }
    }

    private fun addStringValidationComponent(components: MutableList<SchemaDefinitionComponent>) {
        val minLength = node.getTextOrNull("minLength")?.toInt()
        val maxLength = node.getTextOrNull("maxLength")?.toInt()
        val pattern = node.getTextOrNull("pattern")
        if (minLength != null || maxLength != null || pattern != null) {
            components.add(StringValidationComponent(minLength, maxLength, pattern))
        }
    }

    private fun ParseContext.addNumberValidationComponent(components: MutableList<SchemaDefinitionComponent>) {
        val minimum = extractComparableNumber("minimum")
        val maximum = extractComparableNumber("maximum")
        if (minimum != null || maximum != null) {
            components.add(NumberValidationComponent(minimum, maximum))
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

    private fun addDefaultComponent(components: MutableList<SchemaDefinitionComponent>) {
        node.getTextOrNull("default")?.let { components.add(DefaultComponent(it)) }
    }

    private fun ParseContext.addAllOfComponent(components: MutableList<SchemaDefinitionComponent>) {
        val schemas = node.withArray("allOf")
            .mapIndexed { idx, it ->
                contextFor(it, "allOf", "$idx").parseAsSchemaUsage()
            }

        if (schemas.isNotEmpty()) {
            components.add(AllOfComponent(schemas))
        }
    }

    private fun ParseContext.addAnyOfComponent(components: MutableList<SchemaDefinitionComponent>) {
        val schemas = node.withArray("anyOf")
            .mapIndexed { idx, it ->
                contextFor(it, "anyOf", "$idx").parseAsSchemaUsage()
            }

        if (schemas.isNotEmpty()) {
            components.add(AnyOfComponent(schemas))
        }
    }

    private fun ParseContext.addOneOfComponent(components: MutableList<SchemaDefinitionComponent>) {
        val schemas = node.withArray("oneOf")
            .mapIndexed { idx, it ->
                contextFor(it, "oneOf", "$idx").parseAsSchemaUsage()
            }

        if (schemas.isNotEmpty()) {
            components.add(OneOfComponent(schemas))
        }
    }

    private fun ParseContext.addObjectComponent(components: MutableList<SchemaDefinitionComponent>) {
        val properties = node.with("properties")
            .propertiesAsList()
            .map { (name, propertyNode) ->
                contextFor(propertyNode, "properties", name).parseAsSchemaProperty(name)
            }

        if (properties.isNotEmpty()) {
            components.add(ObjectComponent(properties))
        }
    }

    private fun addObjectValidationComponent(components: MutableList<SchemaDefinitionComponent>) {
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

fun ParseContext.parseAsSchemaDefinitionInto(schemaDefinition: TransformableSchemaDefinition) {
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaDefinitionBuilder(schemaDefinition, it).run { this@parseAsSchemaDefinitionInto.build() }
        }
}
