package com.ancientlightstudios.quarkus.kotlin.openapi

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

@Suppress("unused")
fun Maybe<String?>.asString(): Maybe<String?> = this

@Suppress("unused")
fun Maybe<String?>.asInt(): Maybe<Int?> = this.mapNotNull("is not an int") { it.toInt() }

@Suppress("unused")
fun Maybe<String?>.asUInt(): Maybe<UInt?> = this.mapNotNull("is not an uint") { it.toUInt() }

@Suppress("unused")
fun Maybe<String?>.asLong(): Maybe<Long?> = this.mapNotNull("is not a long") { it.toLong() }

@Suppress("unused")
fun Maybe<String?>.asULong(): Maybe<ULong?> = this.mapNotNull("is not an ulong") { it.toULong() }

@Suppress("unused")
fun Maybe<String?>.asBigInteger(): Maybe<BigInteger?> = this.mapNotNull("is not a valid integer") { it.toBigInteger() }

@Suppress("unused")
fun Maybe<String?>.asFloat(): Maybe<Float?> = this.mapNotNull("is not a float") { it.toFloat() }

@Suppress("unused")
fun Maybe<String?>.asDouble(): Maybe<Double?> = this.mapNotNull("is not a double") { it.toDouble() }

fun Maybe<String?>.asBigDecimal(): Maybe<BigDecimal?> = this.mapNotNull("is not a valid decimal") { it.toBigDecimal() }

@Suppress("unused")
fun Maybe<String?>.asBoolean(): Maybe<Boolean?> = this.mapNotNull("is not a boolean") { it.toBooleanStrict()
    when(val value = it.lowercase()) {
        "true" -> true
        "false"-> false
        else ->  throw IllegalArgumentException()
    }
}

@Suppress("unused")
fun Maybe<String?>.asByteArray(): Maybe<ByteArray?> = this.mapNotNull("is not a valid base64 encoded byte array") {
    Base64.getDecoder().decode(it)
}

