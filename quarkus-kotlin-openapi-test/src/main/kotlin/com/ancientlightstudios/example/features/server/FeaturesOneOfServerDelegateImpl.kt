package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesOneOfServerDelegateImpl : FeaturesOneOfServerDelegate {

    override suspend fun OneOfTest1Response.oneOfTest1(request: Maybe<OneOfTest1Request>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun OneOfTest2Response.oneOfTest2(request: Maybe<OneOfTest2Request>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun OneOfTest3Response.oneOfTest3(request: Maybe<OneOfTest3Request>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }
}