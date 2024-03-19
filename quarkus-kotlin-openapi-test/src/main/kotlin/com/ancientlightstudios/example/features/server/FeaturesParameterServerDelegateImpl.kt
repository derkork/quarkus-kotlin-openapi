package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesParameterServerDelegateImpl : FeaturesParametersServerDelegate {

    override suspend fun parametersTest1(request: Maybe<ParametersTest1Request>): ParametersTest1Response {
        val validRequest = request.validOrElse { return ParametersTest1Response.badRequest(it.toOperationError()) }

        return ParametersTest1Response.noContent(validRequest.first, validRequest.second, validRequest.xThirdHeader)
    }

    override suspend fun parametersTest2(request: Maybe<ParametersTest2Request>): ParametersTest2Response {
        val validRequest = request.validOrElse { return ParametersTest2Response.badRequest(it.toOperationError()) }

        return ParametersTest2Response.noContent(validRequest.first, validRequest.xSecondHeader)
    }
}