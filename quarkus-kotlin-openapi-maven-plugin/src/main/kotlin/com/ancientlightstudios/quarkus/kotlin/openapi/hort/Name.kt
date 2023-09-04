package com.ancientlightstudios.quarkus.kotlin.openapi.hort

sealed class Name {
    class ClassName(val name: String) : Name()
}