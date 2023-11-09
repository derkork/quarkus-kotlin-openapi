package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode

/**
 * returns this node as an object node if possible or throws an IllegalStateException
 */
fun JsonNode?.asObjectNode(message: () -> String) =
    when {
        this != null && this.isObject -> this as ObjectNode
        else -> throw IllegalStateException(message())
    }

/**
 * returns this node as an array node if possible or throws an IllegalStateException
 */
fun JsonNode?.asArrayNode(message: () -> String) =
    when {
        this != null && this.isArray -> this as ArrayNode
        else -> throw IllegalStateException(message())
    }

/**
 * returns the properties of this object node as a list of name-value pairs
 */
fun ObjectNode.propertiesAsList(): List<Pair<String, JsonNode>> {
    val result = mutableListOf<Pair<String, JsonNode>>()
    val names = this.fieldNames()
    names.forEach {
        result.add(it to this[it])
    }
    return result
}

/**
 * returns the text value of the specified property or null if the property or node itself is not available
 */
fun JsonNode?.getTextOrNull(property: String): String? = this?.get(property)?.asText()

/**
 * returns the boolean value of the specified property or null if the property or node itself is not available
 */
fun JsonNode?.getBooleanOrNull(property: String): Boolean? = this?.get(property)?.asBoolean()

/**
 * returns the value of a property. Supports array properties as well as single-value properties. Returns
 * null if the property doesn't exist or the current node is null
 */
fun JsonNode?.getMultiValue(property: String) = when (val node = this?.get(property)) {
    null -> null
    is ArrayNode -> node.toList()
    is ValueNode -> listOf(node)
    else -> throw IllegalArgumentException("${node.javaClass.simpleName} not supported")
}


//fun JsonNode?.getAsObjectNode(property: String): ObjectNode =
//    this?.get(property) as? ObjectNode ?: throw IllegalArgumentException("$property is not of type ObjectNode")
//
private val PathPattern = Regex("(?<!\\\\)/").toPattern()

fun JsonNode?.resolvePointer(path: String): JsonNode? {
    val foo = path.replaceFirst("#/", "")

    // we split at / and then walk the tree. We can escape / with \/.
    var result = this
    for (segment in foo.split(PathPattern)) {
        val cleanSegment = segment.replace("\\/", "/")
        result = result?.get(cleanSegment)
    }

    return result
}
