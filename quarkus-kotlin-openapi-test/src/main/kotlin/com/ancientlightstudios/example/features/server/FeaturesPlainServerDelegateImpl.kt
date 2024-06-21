package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesPlainServerDelegateImpl : FeaturesPlainServerDelegate {

    override suspend fun PlainEnumParameterContext.plainEnumParameter(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.param)
    }

    override suspend fun PlainEnumBodyContext.plainEnumBody(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

}