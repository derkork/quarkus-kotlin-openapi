package com.tallence.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun JsonNode?.getTextOrNull(property: String): String? = this?.get(property)?.asText()

fun JsonNode?.getBooleanOrNull(property: String): Boolean? = this?.get(property)?.asBoolean()

fun JsonNode?.getAsObjectNode(property: String): ObjectNode =
    this?.get(property) as? ObjectNode ?: throw IllegalArgumentException("$property is not of type OpbjectNode")

private val PathPattern = Regex("(?<!\\\\)/").toPattern()

fun JsonNode?.resolvePath(path: String): JsonNode? {
    val foo = path.replaceFirst("#/", "")

    // we split at / and then walk the tree. We can escape / with \/.
    var result = this
    for (segment in foo.split(PathPattern)) {
        val cleanSegment = segment.replace("\\/", "/")
        result = result?.get(cleanSegment)
    }

    return result
}
