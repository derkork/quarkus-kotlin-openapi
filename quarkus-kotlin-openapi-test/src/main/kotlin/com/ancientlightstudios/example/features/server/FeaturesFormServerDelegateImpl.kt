package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.SimpleEnum
import com.ancientlightstudios.example.features.server.model.SimpleForm
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesFormServerDelegateImpl : FeaturesFormServerDelegate {

    override suspend fun FormRequiredObjectResponse.formRequiredObject(request: Maybe<FormRequiredObjectRequest>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

}