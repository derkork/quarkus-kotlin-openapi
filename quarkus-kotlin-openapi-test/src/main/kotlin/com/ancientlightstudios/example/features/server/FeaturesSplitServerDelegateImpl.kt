package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.ObjectWithReadOnlyPropertyDown
import com.ancientlightstudios.example.features.server.model.ReadOnlyObject
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesSplitServerDelegateImpl : FeaturesSplitServerDelegate {

    override suspend fun SplitTest1Response.splitTest1(request: Maybe<SplitTest1Request>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(ObjectWithReadOnlyPropertyDown("foo", validRequest.body?.normalProperty ?: "bar"))
    }

    override suspend fun SplitTest2Response.splitTest2(request: Maybe<SplitTest2Request>): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        val body = validRequest.body

        ok(ReadOnlyObject(body.writeOnlyProperty, body.normalProperty))
    }

}