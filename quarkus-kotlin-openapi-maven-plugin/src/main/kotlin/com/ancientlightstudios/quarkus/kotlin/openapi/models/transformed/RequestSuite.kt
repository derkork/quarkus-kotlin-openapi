package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

data class RequestSuite(
    val name: ClassName,
    val version: String?,
    val requests: List<Request>,
    val additionalInformation: AdditionalInformation
)