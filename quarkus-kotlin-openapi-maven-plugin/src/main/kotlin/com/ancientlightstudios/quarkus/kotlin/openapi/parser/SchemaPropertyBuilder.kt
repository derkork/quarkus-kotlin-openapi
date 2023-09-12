package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaProperty
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

class SchemaPropertyBuilder(
    private val name: String,
    private val node: ObjectNode,
    private val requiredList: List<String>,
    private val schemaRegistry: SchemaRegistry,
    private val typeNameHint: () -> String
) {
    
    fun build(): SchemaProperty {
        val type = node.extractSchemaRef(schemaRegistry, typeNameHint)
        val required = requiredList.contains(name)

        val validationInfo = ValidationInfo(required)
        return SchemaProperty(name, type, validationInfo)
    }

}

fun JsonNode.parseAsSchemaProperty(name: String, schemaRegistry: SchemaRegistry, requiredList:List<String>, typeNameHint:() -> String): SchemaProperty {
    require(this.isObject) { "Json object expected" }

    return SchemaPropertyBuilder(name, this as ObjectNode, requiredList, schemaRegistry, typeNameHint).build()
}