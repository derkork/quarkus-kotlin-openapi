package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

//@ApplicationScoped
//class FeaturesOneOfServerDelegateImpl : FeaturesOneOfServerDelegate {
//
//    override suspend fun OneOfTest1Context.oneOfTest1(): Nothing {
//        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
//
//        ok(validRequest.body)
//    }
//
//    override suspend fun OneOfTest2Context.oneOfTest2(): Nothing {
//        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
//
//        ok(validRequest.body)
//    }
//
//    override suspend fun OneOfTest3Context.oneOfTest3(): Nothing {
//        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
//
//        ok(validRequest.body)
//    }
//}