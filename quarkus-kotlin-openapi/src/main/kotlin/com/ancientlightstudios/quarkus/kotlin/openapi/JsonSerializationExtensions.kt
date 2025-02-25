package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger

private val log: Logger = LoggerFactory.getLogger("com.ancientlightstudios.quarkus.kotlin.openapi.JsonSerializationExtensions")

private val factory = JsonNodeFactory.instance

fun JsonNode?.asString(objectMapper: ObjectMapper) : String = when(this) {
    null, is NullNode -> ""
    else -> objectMapper.writeValueAsString(this)
}

fun String.asJson(): JsonNode = factory.textNode(this)

fun Int.asJson(): JsonNode = factory.numberNode(this)

fun UInt.asJson(): JsonNode = factory.numberNode(this.toLong())

fun Long.asJson(): JsonNode = factory.numberNode(this)

fun ULong.asJson(): JsonNode = factory.numberNode(BigDecimal(this.toString()))

fun BigInteger.asJson(): JsonNode = factory.numberNode(this)

// Serialize floats + doubles as BigDecimal so we can use Jackson's setting to have non-scientific notation
// for floats and doubles without having to do any additional annotations of our model or registering custom
// serializers.
fun Float.asJson(): JsonNode  {
    if (this.isNaN()) {
        return factory.numberNode(this)
    }
    return when(this) {
        Float.POSITIVE_INFINITY , Float.NEGATIVE_INFINITY -> factory.numberNode(this)
        else -> factory.numberNode(this.toBigDecimal())
    }
}

fun Double.asJson(): JsonNode {
    if (this.isNaN()) {
        return factory.numberNode(this)
    }
    return when(this) {
        Double.POSITIVE_INFINITY , Double.NEGATIVE_INFINITY -> factory.numberNode(this)
        else -> factory.numberNode(this.toBigDecimal())
    }
}

fun BigDecimal.asJson(): JsonNode = factory.numberNode(this)

fun Boolean.asJson(): JsonNode = factory.booleanNode(this)

fun ByteArray.asJson() : JsonNode = this.asString().asJson()

fun <T> Map<String, T>.asJson(block: (T) -> JsonNode): JsonNode =
    factory.objectNode().apply {
        this@asJson.forEach { set<JsonNode>(it.key, block(it.value)) }
    }

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

fun <T> ObjectNode.setAdditionalProperties(properties: Map<String, T>?, vararg ignoredProperties: String, block: (T) -> JsonNode?): ObjectNode {
    if (properties == null) {
        return this
    }
    
    val ignore = ignoredProperties.toSet()
    val duplicates = properties.keys.intersect(ignore)
    if (duplicates.isNotEmpty()) {
        log.info("the following additional properties are ignored because they would shadow real properties from the object: ${duplicates.joinToString()}")
    }

    val transformedProperties = properties
        .filterKeys { it !in ignore }
        .mapValues { (_, value) -> block(value) }

    return setAll(transformedProperties)
}

