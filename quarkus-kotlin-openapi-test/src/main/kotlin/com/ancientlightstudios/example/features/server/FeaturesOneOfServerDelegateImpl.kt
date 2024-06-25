package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesOneOfServerDelegateImpl : FeaturesOneOfServerDelegate {

    override suspend fun OneOfWithoutDiscriminatorContext.oneOfWithoutDiscriminator(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun OneOfWithDiscriminatorContext.oneOfWithDiscriminator(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun OneOfWithDiscriminatorAndMappingContext.oneOfWithDiscriminatorAndMapping(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }
}