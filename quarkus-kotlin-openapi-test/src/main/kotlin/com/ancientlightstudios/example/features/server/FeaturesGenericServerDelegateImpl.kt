package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.*
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

    override suspend fun ResponseWithInterfaceContext.responseWithInterface(): Nothing {
        ok()
    }

    override suspend fun EchoContext.echo(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(validRequest.body)
    }

    override suspend fun ObjectExtensionTestContext.objectExtensionTest(): Nothing {
        ok(BaseObjectExtension("p1", "p2"))
    }

    override suspend fun SplitTest1Context.splitTest1(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(ObjectWithReadOnlyPropertyDown("foo", validRequest.body?.normalProperty ?: "bar"))
    }

    override suspend fun SplitTest2Context.splitTest2(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        val body = validRequest.body

        ok(ReadOnlyObject(body.writeOnlyProperty, body.normalProperty))
    }

    override suspend fun RawHeadersContext.rawHeaders(): Nothing {
        ok(RawHeaders200Response(
            rawHeaderValue("singleValueHeader1"),
            rawHeaderValues("multiValueHeader1"),
            rawHeaderValue("singleValueHeader2"),
            rawHeaderValues("multiValueHeader2")
        ))
    }
}