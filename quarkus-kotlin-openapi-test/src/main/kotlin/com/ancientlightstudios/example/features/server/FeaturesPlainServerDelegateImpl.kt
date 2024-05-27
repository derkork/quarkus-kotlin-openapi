package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.SimpleEnum
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesPlainServerDelegateImpl : FeaturesPlainServerDelegate {

    override suspend fun PlainOptionalEnumResponse.plainOptionalEnum(request: Maybe<PlainOptionalEnumRequest>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun PlainRequiredEnumResponse.plainRequiredEnum(request: Maybe<PlainRequiredEnumRequest>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

}