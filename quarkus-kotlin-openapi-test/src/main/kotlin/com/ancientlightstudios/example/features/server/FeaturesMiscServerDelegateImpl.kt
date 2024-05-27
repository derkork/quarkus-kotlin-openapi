package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.BaseObjectExtension
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesMiscServerDelegateImpl : FeaturesMiscServerDelegate {

    override suspend fun UnknownStatusCodeContext.unknownStatusCode(): Nothing {
        status422()
    }

    override suspend fun UnknownStatusCode2Context.unknownStatusCode2(): Nothing {
        status422()
    }

    override suspend fun ObjectExtensionTestContext.objectExtensionTest(): Nothing {
        ok(BaseObjectExtension("foo", "bar"))
    }
}