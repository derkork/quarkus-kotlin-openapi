package com.ancientlightstudios.quarkus.kotlin.openapi.verify

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec

interface Verifier {

    fun verify(apiSpec: ApiSpec)
    
}