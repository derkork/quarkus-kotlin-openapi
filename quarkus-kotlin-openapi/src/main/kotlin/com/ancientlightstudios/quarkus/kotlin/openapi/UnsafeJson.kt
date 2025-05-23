package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

class UnsafeJson<T>(private val value: JsonNode) {

    fun asJson() = value

    @Suppress("unused")
    fun modifyObject(block: ObjectNode.() -> ObjectNode) = when (value) {
        is ObjectNode -> UnsafeJson<T>(value.run(block))
        else -> throw IllegalStateException("object node expected")
    }

    @Suppress("unused")
    fun modifyArray(block: ArrayNode.() -> ArrayNode) = when (value) {
        is ArrayNode -> UnsafeJson<T>(value.run(block))
        else -> throw IllegalStateException("array node expected")
    }
}