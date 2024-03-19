package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesParameterServerDelegateImpl : FeaturesParametersServerDelegate {

    override suspend fun parametersTest(request: Maybe<ParametersTestRequest>): ParametersTestResponse {
        val validRequest = request.validOrElse { return ParametersTestResponse.badRequest(it.toOperationError()) }

        return ParametersTestResponse.noContent(validRequest.first, validRequest.second, validRequest.xThirdHeader)
    }

}