package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesParameterServerDelegateImpl : FeaturesParametersServerDelegate {

    override suspend fun ParametersTest1Context.parametersTest1(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        noContent(validRequest.first, validRequest.second, validRequest.xThirdHeader)
    }

    override suspend fun ParametersTest2Context.parametersTest2(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        noContent(validRequest.first, validRequest.xSecondHeader)
    }
}