package com.ancientlightstudios.quarkus.kotlin.openapi

import org.jboss.resteasy.reactive.RestResponse

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
fun Float.asString() = this.toString()

@Suppress("unused")
fun Double.asString() = this.toString()

@Suppress("unused")
fun Boolean.asString() = this.toString()

fun <T> RestResponse.ResponseBuilder<T>.headers(name: String, value: Any?) : RestResponse.ResponseBuilder<T> {
    when(value) {
        is Collection<*> -> value.forEach { header(name, it) }
        else -> header(name, value)
    }
    return this
}
