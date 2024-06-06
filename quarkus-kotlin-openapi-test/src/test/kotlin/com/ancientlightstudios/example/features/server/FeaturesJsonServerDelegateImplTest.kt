package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.ApiTestBase
import com.ancientlightstudios.example.features.testclient.FeaturesJsonTestClient
import com.ancientlightstudios.example.features.testclient.model.SimpleEnum
import com.ancientlightstudios.example.features.testclient.model.SimpleObject
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@QuarkusTest
class FeaturesJsonServerDelegateImplTest : ApiTestBase() {

    val client: FeaturesJsonTestClient
        get() = FeaturesJsonTestClient(objectMapper) { prepareRequest() }

    @Test
    fun `sending the wrong content type is rejected`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/json/required/object")
            .then()
            .statusCode(415)
    }

    @Test
    fun `not sending a body is rejected`() {
        client.jsonRequiredObjectRaw {
            this.contentType("application/json")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly("request.body: is not valid json")
        }
    }

    @Test
    fun `sending the wrong value is rejected`() {
        client.jsonRequiredObjectRaw {
            this.contentType("application/json")
                .body("true")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly("request.body: is not a valid json object")
        }
    }

    @Test
    fun `sending null as an optional body is accepted`() {
        client.jsonOptionalObjectSafe(null)
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending a valid value is accepted`() {
        client.jsonRequiredObjectSafe(
            SimpleObject(statusRequired = SimpleEnum.First, itemsRequired = listOf("one"))
        ).isOkResponse {
            assertThat(safeBody.nameOptional).isEqualTo("i am optional")
            assertThat(safeBody.nameRequired).isEqualTo("i am required")
            assertThat(safeBody.statusOptional).isNull()
            assertThat(safeBody.statusRequired).isEqualTo(SimpleEnum.First)
            assertThat(safeBody.itemsOptional).isNull()
            assertThat(safeBody.itemsRequired).containsExactly("one")
        }
    }
}
