package com.ancientlightstudios.example.custom

import com.ancientlightstudios.quarkus.kotlin.openapi.DefaultValidator
import com.ancientlightstudios.quarkus.kotlin.openapi.ErrorKind

fun DefaultValidator.withO(value: String) {
    if (!value.contains('o', ignoreCase = true)) {
        fail("must contain the letter 'o'", ErrorKind.Invalid)
    }
}

fun DefaultValidator.allLower(value: String) {
    if (value.lowercase() != value) {
        fail("must only be lowercase", ErrorKind.Invalid)
    }
}
