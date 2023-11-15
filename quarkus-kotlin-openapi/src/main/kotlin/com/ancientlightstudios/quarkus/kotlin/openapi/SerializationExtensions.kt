package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal


private val factory = JsonNodeFactory.instance

fun String.fromString(): JsonNode = factory.textNode(this)

fun Int.fromInt(): JsonNode = factory.numberNode(this)

fun UInt.fromUInt(): JsonNode = factory.numberNode(this.toLong())

fun Long.fromLong(): JsonNode = factory.numberNode(this)

fun ULong.fromULong(): JsonNode = factory.numberNode(BigDecimal(this.toString()))

fun Double.fromDouble(): JsonNode = factory.numberNode(this)

fun Float.fromFloat(): JsonNode = factory.numberNode(this)

fun Boolean.fromBoolean(): JsonNode = factory.booleanNode(this)


fun <T> List<T>.fromList(block: (T) -> JsonNode): JsonNode =
    factory.arrayNode().apply {
        this@fromList.forEach { add(block(it)) }
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

