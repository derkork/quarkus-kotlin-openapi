package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.parameter.Parameter

data class Request(
    val path: String,
    val method: RequestMethod,
    val operationId: String,
    val description: String?,
    val deprecated: Boolean,
    val parameters: List<Parameter>,
    val body: RequestBody?,
    val responses: List<Pair<ResponseCode, ResponseBody>>
)
