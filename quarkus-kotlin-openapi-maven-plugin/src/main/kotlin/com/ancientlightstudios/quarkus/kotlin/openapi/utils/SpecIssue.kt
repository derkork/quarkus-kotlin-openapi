package com.ancientlightstudios.quarkus.kotlin.openapi.utils

fun SpecIssue(message:String):Nothing = throw IllegalStateException("Spec issue: $message")