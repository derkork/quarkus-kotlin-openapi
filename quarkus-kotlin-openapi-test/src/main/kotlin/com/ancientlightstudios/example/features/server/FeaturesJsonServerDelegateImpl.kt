package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesJsonServerDelegateImpl : FeaturesJsonServerDelegate {

    override suspend fun JsonOptionalObjectResponse.jsonOptionalObject(request: Maybe<JsonOptionalObjectRequest>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun JsonRequiredObjectResponse.jsonRequiredObject(request: Maybe<JsonRequiredObjectRequest>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }
}