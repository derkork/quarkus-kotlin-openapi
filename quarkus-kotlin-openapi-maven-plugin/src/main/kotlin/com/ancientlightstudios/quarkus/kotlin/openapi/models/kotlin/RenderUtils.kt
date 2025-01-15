package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

fun KotlinTypeReference.render(): String {
    val postfix = when (nullable) {
        true -> "?"
        else -> ""
    }

    return when (this) {
        is KotlinSimpleTypeReference -> "$name$postfix"
        is KotlinParameterizedTypeReference -> innerTypes.joinToString(
            prefix = "${outerType.name}<", postfix = ">$postfix"
        ) { it.render() }

        is KotlinDelegateTypeReference -> {
            val receiverPart = receiver?.let { "${it.render()}." } ?: ""
            val parameterPart = parameters.joinToString { it.render() }
            val resultPart = returnType.render()
            
            "$receiverPart($parameterPart) -> $resultPart"
        }
    }
}