package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.BaseObjectExtension
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesMiscServerDelegateImpl : FeaturesMiscServerDelegate {

    override suspend fun UnknownStatusCodeResponse.unknownStatusCode(): Nothing {
        status422()
    }

    override suspend fun UnknownStatusCode2Response.unknownStatusCode2(): Nothing {
        status422()
    }

    override suspend fun ObjectExtensionTestResponse.objectExtensionTest(): Nothing {
        ok(BaseObjectExtension("foo", "bar"))
    }
}