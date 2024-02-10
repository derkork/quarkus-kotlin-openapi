package com.ancientlightstudios.quarkus.kotlin.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

@Suppress("unused")
fun Maybe<String?>.asString(): Maybe<String?> = this

@Suppress("unused")
fun Maybe<String?>.asFloat(): Maybe<Float?> = this.mapNotNull("is not a float") { it.toFloat() }

@Suppress("unused")
fun Maybe<String?>.asDouble(): Maybe<Double?> = this.mapNotNull("is not a double") { it.toDouble() }

@Suppress("unused")
fun Maybe<String?>.asInt(): Maybe<Int?> = this.mapNotNull("is not an int") { it.toInt() }

@Suppress("unused")
fun Maybe<String?>.asUInt(): Maybe<UInt?> = this.mapNotNull("is not an uint") { it.toUInt() }

@Suppress("unused")
fun Maybe<String?>.asULong(): Maybe<ULong?> = this.mapNotNull("is not an ulong") { it.toULong() }

@Suppress("unused")
fun Maybe<String?>.asLong(): Maybe<Long?> = this.mapNotNull("is not a long") { it.toLong() }

@Suppress("unused")
fun Maybe<String?>.asBoolean(): Maybe<Boolean?> = this.mapNotNull("is not a boolean") { it.toBoolean() }
