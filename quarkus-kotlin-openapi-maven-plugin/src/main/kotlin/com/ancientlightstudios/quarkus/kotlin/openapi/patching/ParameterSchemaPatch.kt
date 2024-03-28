package com.ancientlightstudios.quarkus.kotlin.openapi.patching

import com.ancientlightstudios.quarkus.kotlin.openapi.parser.asObjectNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider

class ParameterSchemaPatch : JsonPatch {

    override fun process(json: JsonNode) {
        Configuration.setDefaults(object : Configuration.Defaults {
            override fun jsonProvider() = JacksonJsonNodeJsonProvider()

            override fun options() = mutableSetOf<Option>(Option.SUPPRESS_EXCEPTIONS)

            override fun mappingProvider() = JacksonMappingProvider()
        })

        // $/components/parameters/*
        JsonPath.read<ArrayNode>(json, "\$.components.parameters[*]").forEach { it.schemaToContent() }
        // $/paths/<endpoints>/parameters/*
        JsonPath.read<ArrayNode>(json, "\$.paths[*].parameters[*]").forEach { it.schemaToContent() }
        // $/paths/<endpoints>/<operation>/parameters/*
        JsonPath.read<ArrayNode>(json, "\$.paths[*][*].parameters[*]").forEach { it.schemaToContent() }

        // $/components/headers/*
        JsonPath.read<ArrayNode>(json, "\$.components.headers[*]").forEach { it.schemaToContent() }
        // $/components/responses/<name>/headers/*
        JsonPath.read<ArrayNode>(json, "\$.components.responses[*].headers[*]").forEach { it.schemaToContent() }
        // $/paths/<endpoints>/<operation>/responses/<name>/headers/*
        JsonPath.read<ArrayNode>(json, "\$.paths[*][*].responses[*].headers[*]").forEach { it.schemaToContent() }
    }

    private fun JsonNode.schemaToContent() {
        // TODO: move style and explode too
        val node = this.asObjectNode { "object node expected" }
        node.remove("schema")?.let {
            node.putObject("content")
                .putObject("text/plain")
                .put("schema", it)
        }
    }

}