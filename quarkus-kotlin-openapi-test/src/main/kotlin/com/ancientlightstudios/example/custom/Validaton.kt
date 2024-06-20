package com.ancientlightstudios.example.custom

import com.ancientlightstudios.quarkus.kotlin.openapi.DefaultValidator

fun DefaultValidator.withO(value: String) {
    if (!value.contains('o', ignoreCase = true)) {
        fail("must contain the letter 'o'")
    }
}

fun DefaultValidator.allLower(value: String) {
    if (value.lowercase() != value) {
        fail("must only be lowercase")
    }
}
