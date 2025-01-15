package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.*
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesPlainServerDelegateImpl : FeaturesPlainServerDelegate {

    override suspend fun PlainBigIntegerTypeContext.plainBigIntegerType(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(PlainBigIntegerType200Response(validRequest.param, validRequest.body))
    }

    override suspend fun PlainIntegerTypeContext.plainIntegerType(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(PlainIntegerType200Response(validRequest.param, validRequest.body))
    }

    override suspend fun PlainBigDecimalTypeContext.plainBigDecimalType(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(PlainBigDecimalType200Response(validRequest.param, validRequest.body))
    }

    override suspend fun PlainFloatingTypeContext.plainFloatingType(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(PlainFloatingType200Response(validRequest.param, validRequest.body))
    }

    override suspend fun PlainBooleanTypeContext.plainBooleanType(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(PlainBooleanType200Response(validRequest.param, validRequest.body))
    }

    override suspend fun PlainStringTypeContext.plainStringType(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(PlainStringType200Response(validRequest.param, validRequest.body))
    }

    override suspend fun PlainEnumTypeContext.plainEnumType(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(PlainEnumType200Response(validRequest.param, validRequest.body))
    }
}