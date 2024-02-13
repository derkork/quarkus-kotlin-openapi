package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.ApiTestBase
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@QuarkusTest
class FeaturesJsonServerDelegateImplTest : ApiTestBase() {

    @Test
    fun `sending the wrong content type is rejected`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/json/optional/object".toTestUrl())
            .execute()
            .statusCode(415)
    }

    @Test
    fun `not sending a body is rejected`() {
        prepareRequest()
            .contentType("application/json")
            .post("/features/json/optional/object".toTestUrl())
            .execute()
            .statusCode(400)
    }

    @Test
    fun `sending the wrong value is rejected`() {
        prepareRequest()
            .contentType("application/json")
            .body("true")
            .post("/features/json/optional/object".toTestUrl())
            .execute()
            .statusCode(400)
    }

    @Test
    fun `sending a valid value is accepted`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                "statusRequired": "first",
                "itemsRequired": ["one"]
            }""".trimIndent()
            )
            .post("/features/json/optional/object".toTestUrl())
            .execute()
            .statusCode(200)
            .withJsonBody {
                assertThat(it.getString("nameOptional")).isEqualTo("i am optional")
                assertThat(it.getString("nameRequired")).isEqualTo("i am required")
                assertThat(it.getString("statusOptional")).isNull()
                assertThat(it.getString("statusRequired")).isEqualTo("first")
                assertThat(it.getList<String>("itemsOptional")).isNull()
                assertThat(it.getList<String>("itemsRequired")).contains("one")
            }
    }
}
