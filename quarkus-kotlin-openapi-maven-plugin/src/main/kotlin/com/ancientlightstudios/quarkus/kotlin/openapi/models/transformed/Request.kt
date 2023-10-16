package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode

data class Request(
    val name: MethodName, val path: String, val method: RequestMethod,
    val parameters: List<Parameter>, val body: TypeName?,
    val responses: List<Pair<ResponseCode, TypeName?>>,
    val additionalInformation: AdditionalInformation
) {

    fun hasInputData() = parameters.isNotEmpty() || body != null

}