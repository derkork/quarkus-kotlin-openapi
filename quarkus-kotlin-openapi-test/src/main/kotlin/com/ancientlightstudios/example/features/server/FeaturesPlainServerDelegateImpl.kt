package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.*
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesPlainServerDelegateImpl : FeaturesPlainServerDelegate {

    override suspend fun PlainEnumParameterContext.plainEnumParameter(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.param)
    }

    override suspend fun PlainEnumBodyContext.plainEnumBody(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun PlainIntegerTypeContext.plainIntegerType(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(PlainIntegerType200Response(validRequest.param, validRequest.body))
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