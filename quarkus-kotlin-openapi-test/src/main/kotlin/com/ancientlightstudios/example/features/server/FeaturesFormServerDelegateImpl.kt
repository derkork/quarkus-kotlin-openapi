package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

//@ApplicationScoped
//class FeaturesFormServerDelegateImpl : FeaturesFormServerDelegate {
//
//    override suspend fun FormRequiredObjectContext.formRequiredObject(): Nothing {
//        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
//
//        ok(validRequest.body)
//    }
//
//}