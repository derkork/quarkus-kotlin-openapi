package com.ancientlightstudios.quarkus.kotlin.openapi.utils

fun <T> MutableSet<T>.pop(): T? = firstOrNull()?.also { remove(it) }

fun <T> MutableSet<T>.pop(block: (T) -> Unit) = pop()?.let(block)