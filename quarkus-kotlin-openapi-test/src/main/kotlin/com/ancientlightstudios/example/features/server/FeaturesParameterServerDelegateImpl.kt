package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesParameterServerDelegateImpl : FeaturesParametersServerDelegate {

    override suspend fun ParametersTest1Response.parametersTest1(request: Maybe<ParametersTest1Request>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        noContent(validRequest.first, validRequest.second, validRequest.xThirdHeader)
    }

    override suspend fun ParametersTest2Response.parametersTest2(request: Maybe<ParametersTest2Request>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        noContent(validRequest.first, validRequest.xSecondHeader)
    }
}