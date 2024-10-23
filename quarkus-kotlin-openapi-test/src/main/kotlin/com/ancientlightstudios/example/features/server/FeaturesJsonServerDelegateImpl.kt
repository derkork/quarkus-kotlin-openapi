package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesJsonServerDelegateImpl : FeaturesJsonServerDelegate {

    override suspend fun JsonOptionalObjectContext.jsonOptionalObject(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun JsonRequiredObjectContext.jsonRequiredObject(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun JsonOptionalArrayContext.jsonOptionalArray(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun JsonRequiredArrayContext.jsonRequiredArray(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun JsonOptionalMapContext.jsonOptionalMap(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun JsonRequiredMapContext.jsonRequiredMap(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }
}