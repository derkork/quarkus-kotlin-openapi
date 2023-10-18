package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ResponseCode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.TypeDefinitionUsage

data class Request(
    val name: MethodName, val path: String, val method: RequestMethod,
    val parameters: List<Parameter>, val body: TypeDefinitionUsage?,
    val responses: List<Pair<ResponseCode, TypeDefinitionUsage?>>,
    val additionalInformation: AdditionalInformation
) {

    fun hasInputData() = parameters.isNotEmpty() || body != null

}