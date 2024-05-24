package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.BaseObjectExtension
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesMiscServerDelegateImpl : FeaturesMiscServerDelegate {

    override suspend fun unknownStatusCode(): UnknownStatusCodeResponse {
        return UnknownStatusCodeResponse.status422()
    }

    override suspend fun unknownStatusCode2(): UnknownStatusCode2Response {
        return UnknownStatusCode2Response.status422()
    }

    override suspend fun objectExtensionTest(): ObjectExtensionTestResponse {
        return ObjectExtensionTestResponse.ok(BaseObjectExtension("foo", "bar"))
    }
}