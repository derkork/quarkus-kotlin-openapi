package com.tallence.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun JsonNode?.getTextOrNull(property: String): String? = this?.get(property)?.asText()

fun JsonNode?.getBooleanOrNull(property: String): Boolean? = this?.get(property)?.asBoolean()

fun JsonNode?.getAsObjectNode(property: String): ObjectNode =
    this?.get(property) as? ObjectNode ?: throw IllegalArgumentException("$property is not of type OpbjectNode")

fun JsonNode?.resolvePath(path: String): JsonNode? {
    val foo = path.replaceFirst("#/", "")

    var result = this
    for (segment in foo.split("/")) {
        result = this?.get(segment)
    }

    return result
}
