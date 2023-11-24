package com.ancientlightstudios.quarkus.kotlin.openapi.verify

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec

class ApiSpecVerifier(private val apiSpec: ApiSpec) {

    fun verify() {
        EnumVerifier().verify(apiSpec)
        OneOfSchemaVerifier().verify(apiSpec)
    }

}