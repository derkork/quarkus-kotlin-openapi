package com.ancientlightstudios.quarkus.kotlin.openapi.parser

private val topLevelObjectMatcher = Regex("#/components/(schemas|parameters|requestBodies|responses|headers)/([^/]+)")

fun String.nameSuggestion() = topLevelObjectMatcher.matchEntire(this)?.groupValues?.last()

