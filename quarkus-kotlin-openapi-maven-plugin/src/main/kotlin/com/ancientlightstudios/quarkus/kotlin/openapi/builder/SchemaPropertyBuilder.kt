package com.ancientlightstudios.quarkus.kotlin.openapi.builder

import com.ancientlightstudios.quarkus.kotlin.openapi.SchemaProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ancientlightstudios.quarkus.kotlin.openapi.getBooleanOrNull

class SchemaPropertyBuilder(
    private val name: String,
    private val node: ObjectNode,
    private val schemaRegistry: SchemaRegistry,
    private val typeNameHint: () -> String
) {
    
    fun build(): SchemaProperty {
        val type = node.extractSchemaRef(schemaRegistry, typeNameHint)
        val required = node.getBooleanOrNull("required") ?: false

        return SchemaProperty(name, type, required)
    }

}

fun JsonNode.parseAsSchemaProperty(name: String, schemaRegistry: SchemaRegistry, typeNameHint:() -> String): SchemaProperty {
    require(this.isObject) { "Json object expected" }

    return SchemaPropertyBuilder(name, this as ObjectNode, schemaRegistry, typeNameHint).build()
}