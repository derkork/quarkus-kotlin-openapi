package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesOctetServerDelegateImpl : FeaturesOctetServerDelegate {

    override suspend fun FileUploadRequiredContext.fileUploadRequired(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
        ok(validRequest.body)
    }

    override suspend fun FileUploadOptionalContext.fileUploadOptional(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
        ok(validRequest.body)
    }

}