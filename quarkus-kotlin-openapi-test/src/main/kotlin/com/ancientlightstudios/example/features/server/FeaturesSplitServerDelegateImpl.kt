package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

//@ApplicationScoped
//class FeaturesSplitServerDelegateImpl : FeaturesSplitServerDelegate {
//
//    override suspend fun SplitTest1Context.splitTest1(): Nothing {
//        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
//
//        ok(ObjectWithReadOnlyPropertyDown("foo", validRequest.body?.normalProperty ?: "bar"))
//    }
//
//    override suspend fun SplitTest2Context.splitTest2(): Nothing {
//        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
//
//        val body = validRequest.body
//
//        ok(ReadOnlyObject(body.writeOnlyProperty, body.normalProperty))
//    }
//
//}