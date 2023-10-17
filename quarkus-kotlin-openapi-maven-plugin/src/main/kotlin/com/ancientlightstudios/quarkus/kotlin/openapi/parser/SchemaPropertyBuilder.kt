package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.SchemaProperty
import com.fasterxml.jackson.databind.node.ObjectNode

class SchemaPropertyBuilder(
    private val required: Boolean,
    private val node: ObjectNode
) {

    fun ParseContext.build(): SchemaProperty {
        val readOnly = node.getBooleanOrNull("readOnly") ?: false
        val writeOnly = node.getBooleanOrNull("writeOnly") ?: false

        val direction = when {
            readOnly && writeOnly -> throw IllegalStateException("Property can't be read-only and write-only at the same time. $contextPath")
            readOnly -> Direction.ReadOnly
            writeOnly -> Direction.WriteOnly
            else -> Direction.ReadAndWrite
        }

        return SchemaProperty(
            parseAsSchema(),
            direction,
            node.getTextOrNull("description"),
            node.getTextOrNull("default"),
            required
        )
    }

}

fun ParseContext.parseAsSchemaProperty(required: Boolean) =
    contextNode.asObjectNode { "Json object expected for $contextPath" }
        .let {
            SchemaPropertyBuilder(required, it).run { this@parseAsSchemaProperty.build() }
        }
