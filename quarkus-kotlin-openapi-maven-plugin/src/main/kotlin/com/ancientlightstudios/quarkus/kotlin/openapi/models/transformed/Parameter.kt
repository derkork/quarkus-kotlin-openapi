package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.TypeDefinitionUsage

data class Parameter(
    val name: String,
    val type: TypeDefinitionUsage,
    val source: Source,
    val additionalInformation: AdditionalInformation
)

enum class Source(val value: String) {
    Path("PathParam"),
    Query("QueryParam"),
    Header("HeaderParam"),
    Cookie("CookieParam")
}