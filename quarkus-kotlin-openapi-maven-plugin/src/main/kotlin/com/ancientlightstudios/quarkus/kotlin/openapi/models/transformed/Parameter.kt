package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

data class Parameter(
    val name: VariableName,
    val type: TypeName,
    val source: Source,
    val additionalInformation: AdditionalInformation
)

enum class Source(val value: String) {
    Path("PATH"),
    Query("QUERY"),
    Header("HEADER"),
    Cookie("COOKIE")
}