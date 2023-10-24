package com.ancientlightstudios.quarkus.kotlin.openapi

import java.util.concurrent.ConcurrentHashMap

object PatternCache {

    // TODO: this is a good candidate for a memory leak. Is there another option? Maybe replace with constants, but they allocate memory too.
    private val cache = ConcurrentHashMap<String, Regex>()

    fun compilePattern(pattern: String): Regex = cache.getOrPut(pattern) { Regex(pattern) }

}