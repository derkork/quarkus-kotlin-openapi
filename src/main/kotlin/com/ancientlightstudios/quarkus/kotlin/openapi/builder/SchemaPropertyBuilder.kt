package com.ancientlightstudios.quarkus.kotlin.openapi.builder

import com.ancientlightstudios.quarkus.kotlin.openapi.SchemaProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ancientlightstudios.quarkus.kotlin.openapi.getBooleanOrNull

class SchemaPropertyBuilder(
    private val name: String,
    private val node: ObjectNode,
    private val schemaRegistry: SchemaRegistry
) {
    
    fun build(): SchemaProperty {
        val type = node.extractSchemaRef(schemaRegistry)
        val required = node.getBooleanOrNull("required") ?: false

        return SchemaProperty(name, type, required, false)
    }

}

fun JsonNode.parseAsSchemaProperty(name: String, schemaRegistry: SchemaRegistry): SchemaProperty {
    require(this.isObject) { "Json object expected" }

    return SchemaPropertyBuilder(name, this as ObjectNode, schemaRegistry).build()
}