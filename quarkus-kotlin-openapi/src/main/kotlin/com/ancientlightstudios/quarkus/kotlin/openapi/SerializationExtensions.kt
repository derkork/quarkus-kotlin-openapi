package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal


private val factory = JsonNodeFactory.instance

fun String.toJsonNode(): JsonNode = factory.textNode(this)

fun Int.toJsonNode(): JsonNode = factory.numberNode(this)

fun UInt.toJsonNode(): JsonNode = factory.numberNode(this.toLong())

fun Long.toJsonNode(): JsonNode = factory.numberNode(this)

fun ULong.toJsonNode(): JsonNode = factory.numberNode(BigDecimal(this.toString()))

fun Double.toJsonNode(): JsonNode = factory.numberNode(this)

fun Float.toJsonNode(): JsonNode = factory.numberNode(this)

fun Boolean.toJsonNode(): JsonNode = factory.booleanNode(this)


fun <T> List<T>.toJsonNode(block: (T) -> JsonNode): JsonNode =
    factory.arrayNode().apply {
        this@toJsonNode.forEach { add(block(it)) }
    }

fun objectNode(): ObjectNode = factory.objectNode()

fun JsonNode?.shallowMerge(other: JsonNode?): JsonNode? {
    return this?.let {
        if (other != null) {
            if (it is ObjectNode && other is ObjectNode) {
                it.setAll<ObjectNode>(other)
                return@let  it
            } else {
               return@let other
            }
        }
        it
    } ?: other
}

fun ObjectNode.setNonNull(name: String, value: JsonNode?): ObjectNode {
    if (value != null && !value.isNull) {
        return this.set(name, value)
    }
    return this
}



inline fun ObjectNode.setAny(name: String, value: JsonNode?): ObjectNode = this.set(name, value)

