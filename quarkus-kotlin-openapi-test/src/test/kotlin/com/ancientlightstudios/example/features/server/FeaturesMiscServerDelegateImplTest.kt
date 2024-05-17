package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.ApiTestBase
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

@QuarkusTest
class FeaturesMiscServerDelegateImplTest : ApiTestBase() {

    @Test
    fun `Status code 422 is handled correctly by the framework`() {
        prepareRequest()
            .contentType("application/json")
            .get("/features/misc/unknownStatusCode".toTestUrl())
            .execute()
            .statusCode(422)
    }

    @Test
    fun `Status code 422 is handled correctly by the framework for the second endpoint`() {
        prepareRequest()
            .contentType("application/json")
            .get("/features/misc/unknownStatusCode2".toTestUrl())
            .execute()
            .statusCode(422)
    }

}