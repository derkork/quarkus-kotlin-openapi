package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

sealed interface Expression {

    fun evaluate(): String

}