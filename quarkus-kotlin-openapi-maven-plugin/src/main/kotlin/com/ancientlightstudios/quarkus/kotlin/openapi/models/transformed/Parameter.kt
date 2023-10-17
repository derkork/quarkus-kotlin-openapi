package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

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