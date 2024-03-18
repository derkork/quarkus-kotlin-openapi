package com.ancientlightstudios.quarkus.kotlin.openapi.utils

fun <K, V> MutableMap<K, V>.pop(): Pair<K, V>? = entries.firstOrNull()?.let {
    remove(it.key)
    it.key to it.value
}

fun <K, V> MutableMap<K, V>.pop(block: (K, V) -> Unit) = pop()?.let { block(it.first, it.second) }