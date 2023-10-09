package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiVersion
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.fasterxml.jackson.databind.node.ObjectNode

class RequestParameterBuilder(private val node: ObjectNode) {

    fun ParseContext.build(): Parameter {
        return when (val ref = node.getTextOrNull("\$ref")) {
            null -> extractParameterDefinition()
            else -> extractParameterReference(ref)
        }
    }

    private fun ParseContext.extractParameterDefinition(): Parameter {
        val name = node.getTextOrNull("name")
            ?: throw IllegalArgumentException("Property 'name' is required for parameter $contextPath")
        val kind =
            node.getTextOrNull("in")
                ?: throw IllegalArgumentException("Property 'in' required for parameter $contextPath")

        val schema = contextFor("schema").parseAsSchema()

        return when (kind) {
            "path" -> extractPathParameterDefinition(name, schema)
            "query" -> extractQueryParameterDefinition(name, schema)
            "header" -> extractHeaderParameterDefinition(name, schema)
            "cookie" -> extractCookieParameterDefinition(name, schema)
            else -> throw IllegalStateException("Unknown type '$kind' for parameter $contextPath")
        }
    }

    private fun ParseContext.extractParameterReference(ref: String): Parameter {
        val (targetName, parameter) = referenceResolver.resolveParameter(ref)
        val description = when (openApiVersion) {
            // not supported in v3.0
            OpenApiVersion.V3_0 -> null
            OpenApiVersion.V3_1 -> node.getTextOrNull("description")
        }

        // extract into functions if other versions support more overrides for parameter references
        return when (parameter) {
            is Parameter.PathParameter -> PathParameterReference(targetName, parameter, description)
            is Parameter.QueryParameter -> QueryParameterReference(targetName, parameter, description)
            is Parameter.HeaderParameter -> HeaderParameterReference(targetName, parameter, description)
            is Parameter.CookieParameter -> CookieParameterReference(targetName, parameter, description)
        }
    }

    private fun extractPathParameterDefinition(name: String, schema: Schema) = PathParameterDefinition(
        name, schema, node.getTextOrNull("description")
    )

    private fun extractQueryParameterDefinition(name: String, schema: Schema) = QueryParameterDefinition(
        name, schema, node.getTextOrNull("description"),
        node.getBooleanOrNull("deprecated") ?: false,
        node.getBooleanOrNull("required") ?: false
    )

    private fun extractHeaderParameterDefinition(name: String, schema: Schema) = HeaderParameterDefinition(
        name, schema, node.getTextOrNull("description"),
        node.getBooleanOrNull("deprecated") ?: false,
        node.getBooleanOrNull("required") ?: false
    )

    private fun extractCookieParameterDefinition(name: String, schema: Schema) = CookieParameterDefinition(
        name, schema, node.getTextOrNull("description"),
        node.getBooleanOrNull("deprecated") ?: false,
        node.getBooleanOrNull("required") ?: false
    )

}

fun ParseContext.parseAsRequestParameter() =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            RequestParameterBuilder(it).run { this@parseAsRequestParameter.build() }
        }
