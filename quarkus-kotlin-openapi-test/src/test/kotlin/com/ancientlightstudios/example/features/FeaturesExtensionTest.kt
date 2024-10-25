package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.FeaturesExtensionClient
import com.ancientlightstudios.example.features.client.InstantExtensionHttpResponse
import com.ancientlightstudios.example.features.client.LocalDateExtensionHttpResponse
import com.ancientlightstudios.example.features.client.UuidExtensionHttpResponse
import com.ancientlightstudios.example.features.testclient.FeaturesExtensionTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.*

@QuarkusTest
class FeaturesExtensionTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesExtensionClient

    val testClient: FeaturesExtensionTestClient
        get() = FeaturesExtensionTestClient(objectMapper) { prepareRequest() }

    @Test
    fun `invalid instant value will be rejected (Test-Client)`() {
        testClient.instantExtensionRaw {
            queryParam("headerValue", "not-an-instant")
                .body("\"not-an-instant\"")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("query.headerValue", "Invalid instant"),
                listOf("body", "Invalid instant"),
            )
        }
    }

    @Test
    fun `invalid instant value will be rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("headerValue", "not-an-instant")
            .contentType("application/json")
            .body("\"not-an-instant\"")
            .post("/features/extension/instant")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("query.headerValue", "Invalid instant"),
            listOf("body", "Invalid instant"),
        )
    }

    @Test
    fun `valid instant value is accepted (Client)`() {
        val instant1 = Instant.now().plusSeconds(5)
        val instant2 = instant1.plusSeconds(30)
        runBlocking {
            val response = client.instantExtension(instant1, instant2)
            if (response is InstantExtensionHttpResponse.Ok) {
                assertThat(response.headerValue).isEqualTo(instant1)
                assertThat(response.safeBody).isEqualTo(instant2)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `valid instant value is accepted (Test-Client)`() {
        val instant1 = Instant.now().plusSeconds(5)
        val instant2 = instant1.plusSeconds(30)

        testClient.instantExtensionSafe(instant1, instant2)
            .isOkResponse {
                assertThat(headerValue).isEqualTo(instant1)
                assertThat(safeBody).isEqualTo(instant2)
            }
    }

    @Test
    fun `valid instant value is accepted (Raw)`() {
        val instant1 = Instant.now().plusSeconds(5)
        val instant2 = instant1.plusSeconds(30)

        prepareRequest()
            .queryParam("headerValue", instant1.toString())
            .contentType("application/json")
            .body("\"$instant2\"")
            .post("/features/extension/instant")
            .execute()
            .statusCode(200)
            .header("headerValue", instant1.toString())
            .body(equalTo("\"$instant2\""))
    }

    @Test
    fun `invalid localDate value will be rejected (Test-Client)`() {
        testClient.localDateExtensionRaw {
            queryParam("headerValue", "not-a-localDate")
                .body("\"not-a-localDate\"")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("query.headerValue", "Invalid date"),
                listOf("body", "Invalid date"),
            )
        }
    }

    @Test
    fun `invalid localDate value will be rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("headerValue", "not-a-localDate")
            .contentType("application/json")
            .body("\"not-a-localDate\"")
            .post("/features/extension/localDate")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("query.headerValue", "Invalid date"),
            listOf("body", "Invalid date"),
        )
    }

    @Test
    fun `valid localDate value is accepted (Client)`() {
        val localDate1 = LocalDate.now()
        val localDate2 = localDate1.plusDays(1)
        runBlocking {
            val response = client.localDateExtension(localDate1, localDate2)
            if (response is LocalDateExtensionHttpResponse.Ok) {
                assertThat(response.headerValue).isEqualTo(localDate1)
                assertThat(response.safeBody).isEqualTo(localDate2)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `valid localDate value is accepted (Test-Client)`() {
        val localDate1 = LocalDate.now()
        val localDate2 = localDate1.plusDays(1)

        testClient.localDateExtensionSafe(localDate1, localDate2)
            .isOkResponse {
                assertThat(headerValue).isEqualTo(localDate1)
                assertThat(safeBody).isEqualTo(localDate2)
            }
    }

    @Test
    fun `valid localDate value is accepted (Raw)`() {
        val localDate1 = LocalDate.now()
        val localDate2 = localDate1.plusDays(1)

        prepareRequest()
            .queryParam("headerValue", localDate1.toString())
            .contentType("application/json")
            .body("\"$localDate2\"")
            .post("/features/extension/localDate")
            .execute()
            .statusCode(200)
            .header("headerValue", localDate1.toString())
            .body(equalTo("\"$localDate2\""))
    }

    @Test
    fun `invalid uuid value will be rejected (Test-Client)`() {
        testClient.uuidExtensionRaw {
            queryParam("headerValue", "not-an-uuid")
                .body("\"not-an-uuid\"")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("query.headerValue", "Invalid UUID"),
                listOf("body", "Invalid UUID"),
            )
        }
    }

    @Test
    fun `invalid uuid value will be rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("headerValue", "not-an-uuid")
            .contentType("application/json")
            .body("\"not-an-uuid\"")
            .post("/features/extension/uuid")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("query.headerValue", "Invalid UUID"),
            listOf("body", "Invalid UUID"),
        )
    }

    @Test
    fun `valid uuid value is accepted (Client)`() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()
        runBlocking {
            val response = client.uuidExtension(uuid1, uuid2)
            if (response is UuidExtensionHttpResponse.Ok) {
                assertThat(response.headerValue).isEqualTo(uuid1)
                assertThat(response.safeBody).isEqualTo(uuid2)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `valid uuid value is accepted (Test-Client)`() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        testClient.uuidExtensionSafe(uuid1, uuid2)
            .isOkResponse {
                assertThat(headerValue).isEqualTo(uuid1)
                assertThat(safeBody).isEqualTo(uuid2)
            }
    }

    @Test
    fun `valid uuid value is accepted (Raw)`() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        prepareRequest()
            .queryParam("headerValue", uuid1.toString())
            .contentType("application/json")
            .body("\"$uuid2\"")
            .post("/features/extension/uuid")
            .execute()
            .statusCode(200)
            .header("headerValue", uuid1.toString())
            .body(equalTo("\"$uuid2\""))
    }

}