package com.ancientlightstudios.quarkus.kotlin.openapi.parser

@Suppress("DataClassPrivateConstructor")
data class JsonPointer private constructor(val path: String) {

    fun append(vararg segments: String): JsonPointer {
        val rootPath = path.trimEnd('/')
        val subPath = segments.joinToString(separator = "/") {
            it.replace("~", "~0")
                .replace("/", "~1")
        }

        return JsonPointer("$rootPath/$subPath")
    }

    companion object {

        fun fromPath(path: String) = JsonPointer(path)

        fun fromSegments(vararg segments: String) = JsonPointer("/").append(*segments)

    }
}
