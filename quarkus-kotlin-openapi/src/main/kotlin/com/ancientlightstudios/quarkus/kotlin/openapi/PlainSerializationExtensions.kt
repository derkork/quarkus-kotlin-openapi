package com.ancientlightstudios.quarkus.kotlin.openapi

import org.jboss.resteasy.reactive.RestResponse
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

@Suppress("unused")
fun String.asString() = this

@Suppress("unused")
fun Int.asString() = this.toString()

@Suppress("unused")
fun UInt.asString() = this.toString()

@Suppress("unused")
fun Long.asString() = this.toString()

@Suppress("unused")
fun ULong.asString() = this.toString()

@Suppress("unused")
fun BigInteger.asString() = this.toString()

@Suppress("unused")
fun Float.asString() = this.toString()

@Suppress("unused")
fun Double.asString() = this.toString()

@Suppress("unused")
fun BigDecimal.asString() = this.toString()

@Suppress("unused")
fun Boolean.asString() = this.toString()

@Suppress("unused")
fun ByteArray.asString(): String = Base64.getEncoder().encodeToString(this)

fun <T> RestResponse.ResponseBuilder<T>.headers(name: String, value: Any?): RestResponse.ResponseBuilder<T> {
    when (value) {
        is Collection<*> -> value.forEach { header(name, it) }
        else -> header(name, value)
    }
    return this
}

@Suppress("unused")
fun String?.nullAsEmptyString() = when (this) {
    null -> ""
    else -> this
}

@Suppress("unused")
fun <T> List<T>?.nullAsEmptyList() = when (this) {
    null -> listOf()
    else -> this
}
