package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.ApiTestBase
import com.ancientlightstudios.example.features.server.model.SimpleEnum
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@QuarkusTest
class FeaturesPlainServerDelegateImplTest : ApiTestBase() {

    @Test
    fun `sending the wrong content type is rejected`() {
        prepareRequest()
            .contentType("application/json")
            .post("/features/plain/optional/enum".toTestUrl())
            .execute()
            .statusCode(415)
    }

    @Test
    fun `not sending a body is rejected`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/plain/optional/enum".toTestUrl())
            .execute()
            .statusCode(400)
    }

    @Test
    fun `sending the wrong value is rejected`() {
        prepareRequest()
            .contentType("text/plain")
            .body("foobar")
            .post("/features/plain/optional/enum".toTestUrl())
            .execute()
            .statusCode(400)
    }

    @Test
    fun `sending a valid value is accepted`() {
        prepareRequest()
            .contentType("text/plain")
            .body(SimpleEnum.First.value)
            .post("/features/plain/optional/enum".toTestUrl())
            .execute()
            .statusCode(200)
            .withStringBody { assertThat(it).contains(SimpleEnum.First.value) }
    }

}