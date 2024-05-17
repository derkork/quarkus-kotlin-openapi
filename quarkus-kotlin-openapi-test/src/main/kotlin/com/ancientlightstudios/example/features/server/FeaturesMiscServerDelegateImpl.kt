package com.ancientlightstudios.example.features.server

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesMiscServerDelegateImpl : FeaturesMiscServerDelegate {

    override suspend fun unknownStatusCode(): UnknownStatusCodeResponse {
        return UnknownStatusCodeResponse.status422()
    }

    override suspend fun unknownStatusCode2(): UnknownStatusCode2Response {
        return UnknownStatusCode2Response.status422()
    }
    
}