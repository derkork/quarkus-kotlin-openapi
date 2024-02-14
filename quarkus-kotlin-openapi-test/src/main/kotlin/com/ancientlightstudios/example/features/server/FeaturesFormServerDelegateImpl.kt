package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.SimpleEnum
import com.ancientlightstudios.example.features.server.model.SimpleForm
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesFormServerDelegateImpl : FeaturesFormServerDelegate {

    override suspend fun formRequiredObject(request: Maybe<FormRequiredObjectRequest>): FormRequiredObjectResponse {
        val validRequest = request.validOrElse { return FormRequiredObjectResponse.badRequest(it.toOperationError()) }

        @Suppress("UNUSED_VARIABLE")
        // explicit type notation to trigger the compiler if the body is suddenly nullable
        val body: SimpleForm = FormRequiredObjectRequest(
            SimpleForm("name", SimpleEnum.First)
        ).body

        return FormRequiredObjectResponse.ok(validRequest.body)
    }
}