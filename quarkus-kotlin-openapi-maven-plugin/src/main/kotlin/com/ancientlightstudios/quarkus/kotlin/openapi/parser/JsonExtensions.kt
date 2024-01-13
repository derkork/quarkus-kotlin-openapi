package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import com.flipkart.zjsonpatch.JsonPatch

/**
 * returns this node as an object node or generates a SpecIssue
 */
fun JsonNode?.asObjectNode(message: () -> String) = this as? ObjectNode ?: SpecIssue(message())

/**
 * returns this node as an array node or generates a SpecIssue
 */
fun JsonNode?.asArrayNode(message: () -> String) = this as? ArrayNode ?: SpecIssue(message())

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
    else -> SpecIssue("${node.javaClass.simpleName} not supported")
}


fun JsonNode?.resolvePointer(pointer: JsonPointer): JsonNode? {
    val path = pointer.path.trimStart('#')
    if (path.contains("#")) {
        SpecIssue("References to external files not yet supported. ${pointer.path}")
    }

    if (this == null) {
        return null
    }

    val result = this.at(path)
    if (result.isMissingNode) {
        return null
    }

    return result
}

fun JsonNode.merge(other: JsonNode) = when (this) {
    is ObjectNode -> merge(other)
    is ArrayNode -> merge(other)
    else -> SpecIssue("Merge for ${this.javaClass.simpleName} not supported")
}

fun ObjectNode.merge(other: JsonNode): ObjectNode {
    val otherObject = other.asObjectNode { "Only objects can be merged into objects" }
    otherObject.propertiesAsList()
        .forEach { (name, value) ->
            when (val current = this[name]) {
                is ObjectNode -> current.merge(value)
                is ArrayNode -> current.merge(value)
                else -> replace(name, value)
            }
        }
    return this
}

fun ArrayNode.merge(other: JsonNode): ArrayNode = addAll(other.asArrayNode { "Only arrays can be merged into arrays" })

/**
 * applies the specified patch to this json node
 */
fun JsonNode.patch(patchNode: JsonNode): JsonNode = JsonPatch.apply(patchNode, this)