package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesOneOfServerDelegateImpl : FeaturesOneOfServerDelegate {

    override suspend fun oneOfTest1(request: Maybe<OneOfTest1Request>): OneOfTest1Response {
        val validRequest = request.validOrElse { return OneOfTest1Response.badRequest(it.toOperationError()) }

        return OneOfTest1Response.ok(validRequest.body)
    }

    override suspend fun oneOfTest2(request: Maybe<OneOfTest2Request>): OneOfTest2Response {
        val validRequest = request.validOrElse { return OneOfTest2Response.badRequest(it.toOperationError()) }

        return OneOfTest2Response.ok(validRequest.body)
    }

    override suspend fun oneOfTest3(request: Maybe<OneOfTest3Request>): OneOfTest3Response {
        val validRequest = request.validOrElse { return OneOfTest3Response.badRequest(it.toOperationError()) }

        return OneOfTest3Response.ok(validRequest.body)
    }
}