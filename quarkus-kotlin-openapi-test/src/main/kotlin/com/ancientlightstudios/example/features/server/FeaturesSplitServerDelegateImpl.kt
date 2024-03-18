package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.ObjectWithReadOnlyPropertyDown
import com.ancientlightstudios.example.features.server.model.ObjectWithReadOnlyPropertyUp
import com.ancientlightstudios.example.features.server.model.ReadOnlyObject
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesSplitServerDelegateImpl : FeaturesSplitServerDelegate {

    override suspend fun splitTest1(request: Maybe<SplitTest1Request>): SplitTest1Response {
        val validRequest = request.validOrElse { return SplitTest1Response.badRequest(it.toOperationError()) }

        @Suppress("UNUSED_VARIABLE")
        // explicit type notation to trigger the compiler if the body is suddenly no longer nullable
        val body: ObjectWithReadOnlyPropertyUp? = SplitTest1Request(null).body

        return SplitTest1Response.ok(ObjectWithReadOnlyPropertyDown("foo", validRequest.body?.normalProperty ?: "bar"))
    }

    override suspend fun splitTest2(request: Maybe<SplitTest2Request>): SplitTest2Response {
        val validRequest = request.validOrElse { return SplitTest2Response.badRequest(it.toOperationError()) }

        val body = validRequest.body

        return SplitTest2Response.ok(ReadOnlyObject(body.writeOnlyProperty, body.normalProperty))
    }

}