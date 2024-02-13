package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.SimpleEnum
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesPlainOptionalServerDelegateImpl : FeaturesPlainOptionalServerDelegate {

    override suspend fun plainOptionalEnum(request: Maybe<PlainOptionalEnumRequest>): PlainOptionalEnumResponse {
        val validRequest = request.validOrElse { return PlainOptionalEnumResponse.badRequest(it.toOperationError()) }

        @Suppress("UNUSED_VARIABLE")
        // explicit type notation to trigger the compiler if the body is suddenly no longer nullable
        val body: SimpleEnum? = PlainOptionalEnumRequest(null).body

        return PlainOptionalEnumResponse.ok(validRequest.body)
    }

}