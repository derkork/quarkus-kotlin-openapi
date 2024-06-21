package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.ResponseCodeHint
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesGenericServerDelegateImpl : FeaturesGenericServerDelegate {

    override suspend fun UnknownStatusCodeContext.unknownStatusCode(): Nothing {
        status422()
    }

    override suspend fun ResponseCodeContext.responseCode(): Nothing {
        val validRequest = request.validOrElse { badRequest() }

        when (val code = validRequest.responseCodeHint) {
            ResponseCodeHint.Ok -> ok()
            ResponseCodeHint.BadRequest -> badRequest()
            else -> status(code.value)
        }
    }

    override suspend fun ResponseCodeWithDefaultContext.responseCodeWithDefault(): Nothing {
        val validRequest = request.validOrElse { badRequest() }

        when (val code = validRequest.responseCodeHint) {
            ResponseCodeHint.Ok -> ok()
            ResponseCodeHint.BadRequest -> badRequest()
            else -> defaultStatus(code.value)
        }
    }

    override suspend fun EchoContext.echo(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

}