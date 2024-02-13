package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.SimpleEnum
import com.ancientlightstudios.example.features.server.model.SimpleObject
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesJsonServerDelegateImpl : FeaturesJsonServerDelegate {

    override suspend fun jsonOptionalObject(request: Maybe<JsonOptionalObjectRequest>): JsonOptionalObjectResponse {
        val validRequest = request.validOrElse { return JsonOptionalObjectResponse.badRequest(it.toOperationError()) }

        @Suppress("UNUSED_VARIABLE")
        // explicit type notation to trigger the compiler if the body is suddenly no longer nullable
        val body: SimpleObject? = JsonOptionalObjectRequest(null).body

        return JsonOptionalObjectResponse.ok(validRequest.body)
    }

    override suspend fun jsonRequiredObject(request: Maybe<JsonRequiredObjectRequest>): JsonRequiredObjectResponse {
        val validRequest = request.validOrElse { return JsonRequiredObjectResponse.badRequest(it.toOperationError()) }

        @Suppress("UNUSED_VARIABLE")
        // explicit type notation to trigger the compiler if the body is suddenly nullable
        val body: SimpleObject = JsonRequiredObjectRequest(
            SimpleObject(statusRequired = SimpleEnum.First, itemsRequired = listOf())
        ).body

        return JsonRequiredObjectResponse.ok(validRequest.body)
    }
}