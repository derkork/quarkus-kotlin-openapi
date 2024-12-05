package com.ancientlightstudios.quarkus.kotlin.openapi.parser

private val topLevelObjectMatcher = Regex("#/components/(schemas|parameters|requestBodies|responses|headers)/([^/]+)")

fun String.referencedComponentName() = topLevelObjectMatcher.matchEntire(this)?.groupValues?.last()

