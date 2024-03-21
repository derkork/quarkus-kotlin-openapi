package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal


private val factory = JsonNodeFactory.instance

fun String.asJson(): JsonNode = factory.textNode(this)

fun Int.asJson(): JsonNode = factory.numberNode(this)

fun UInt.asJson(): JsonNode = factory.numberNode(this.toLong())

fun Long.asJson(): JsonNode = factory.numberNode(this)

fun ULong.asJson(): JsonNode = factory.numberNode(BigDecimal(this.toString()))

fun Float.asJson(): JsonNode = factory.numberNode(this)

fun Double.asJson(): JsonNode = factory.numberNode(this)

fun Boolean.asJson(): JsonNode = factory.booleanNode(this)

fun ByteArray.asJson() : JsonNode = this.asString().asJson()

fun <T> List<T>.asJson(block: (T) -> JsonNode): JsonNode =
    factory.arrayNode().apply {
        this@asJson.forEach { add(block(it)) }
    }

fun objectNode(): ObjectNode = factory.objectNode()

fun ObjectNode.setProperty(name: String, value: JsonNode?, required: Boolean): ObjectNode {
    // remove null values from the response, unless they are required and must be included
    if ((value == null || value.isNull) && !required) {
        return this
    }

    return this.set(name, value)
}


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

