package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

data class RequestSuite(
    val name: ClassName,
    val version: String?,
    val requests: List<Request>,
    val additionalInformation: AdditionalInformation
)