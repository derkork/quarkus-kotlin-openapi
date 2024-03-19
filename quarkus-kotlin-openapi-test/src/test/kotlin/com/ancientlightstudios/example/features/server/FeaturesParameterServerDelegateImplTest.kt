package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.ApiTestBase
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

@QuarkusTest
class FeaturesParameterServerDelegateImplTest : ApiTestBase() {

    @Test
    fun `not sending the required parameter is rejected`() {
        prepareRequest()
            .get("/features/parameters/test1".toTestUrl())
            .execute()
            .statusCode(400)
    }

    @Test
    fun `not sending the optional parameter is accepted`() {
        prepareRequest()
            .queryParam("first", "foo")
            .get("/features/parameters/test1".toTestUrl())
            .execute()
            .statusCode(204)
            .header("X-FIRST-HEADER", "foo")
    }

    @Test
    fun `sending the optional parameter is accepted`() {
        prepareRequest()
            .queryParam("first", "foo")
            .queryParam("second", 10)
            .get("/features/parameters/test1".toTestUrl())
            .execute()
            .statusCode(204)
            .header("X-FIRST-HEADER", "foo")
            .header("X-SECOND-HEADER", "10")
    }

    @Test
    fun `sending the wrong parameter value is rejected`() {
        prepareRequest()
            .queryParam("first", "foo")
            .queryParam("second", "foobar")
            .get("/features/parameters/test1".toTestUrl())
            .execute()
            .statusCode(400)
    }

}