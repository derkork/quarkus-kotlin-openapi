package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesExtensionServerDelegateImpl : FeaturesExtensionServerDelegate {

    override suspend fun InstantExtensionContext.instantExtension(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body, validRequest.headerValue)
    }

    override suspend fun UuidExtensionContext.uuidExtension(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body, validRequest.headerValue)
    }
}