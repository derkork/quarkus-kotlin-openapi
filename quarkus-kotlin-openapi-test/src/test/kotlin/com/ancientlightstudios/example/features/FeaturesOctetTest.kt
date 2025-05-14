package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.FeaturesOctetClient
import com.ancientlightstudios.example.features.client.FileUploadOptionalHttpResponse
import com.ancientlightstudios.example.features.client.FileUploadRequiredHttpResponse
import com.ancientlightstudios.example.features.testclient.FeaturesOctetTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@QuarkusTest
class FeaturesOctetTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesOctetClient

    val testClient: FeaturesOctetTestClient
        get() = FeaturesOctetTestClient(dependencyContainer) { prepareRequest() }

    @Test
    fun `empty body is supported for optional content (Client)`() {
        runBlocking {
            val response = client.fileUploadOptional(byteArrayOf())
            if (response is FileUploadOptionalHttpResponse.Ok) {
                assertThat(response.safeBody).isEqualTo(byteArrayOf())
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `empty body is supported for optional content (Test-Client)`() {
        testClient.fileUploadOptionalSafe(byteArrayOf())
            .isOkResponse {
                assertThat(safeBody).isEqualTo(byteArrayOf())
            }
    }

    @Test
    fun `empty body is supported for optional content (Raw)`() {
        val body = prepareRequest()
            .contentType("application/octet-stream")
            .body(byteArrayOf())
            .post("/features/octet/optional/fileUpload")
            .execute()
            .statusCode(200)
            .extract()
            .asByteArray()
        assertThat(body).isEqualTo(byteArrayOf())
    }

    @Test
    fun `null body is supported for optional content (Client)`() {
        runBlocking {
            val response = client.fileUploadOptional(null)
            if (response is FileUploadOptionalHttpResponse.Ok) {
                assertThat(response.safeBody).isEqualTo(byteArrayOf())
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `null body is supported for optional content (Test-Client)`() {
        testClient.fileUploadOptionalSafe(null)
            .isOkResponse {
                assertThat(safeBody).isEqualTo(byteArrayOf())
            }
    }

    @Test
    fun `null body is supported for optional content (Raw)`() {
        val body = prepareRequest()
            .contentType("application/octet-stream")
            .post("/features/octet/optional/fileUpload")
            .execute()
            .statusCode(200)
            .extract()
            .asByteArray()
        assertThat(body).isEqualTo(byteArrayOf())
    }

    @Test
    fun `empty body is rejected when required (Client)`() {
        runBlocking {
            val response = client.fileUploadRequired(byteArrayOf())
            if (response is FileUploadRequiredHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages).containsExactly(listOf("body", "minimum"))
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `empty body is rejected when required (Test-Client)`() {
        testClient.fileUploadRequiredSafe(byteArrayOf())
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("body", "minimum"))
            }
    }

    @Test
    fun `empty body is rejected when required (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/octet-stream")
            .body(byteArrayOf())
            .post("/features/octet/required/fileUpload")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("body", "minimum"))
    }

    @Test
    fun `null body is rejected when required (Test-Client)`() {
        testClient.fileUploadRequiredUnsafe {
            body(null)
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("body", "minimum"))
            }
    }

    @Test
    fun `null body is rejected when required (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/octet-stream")
            .post("/features/octet/required/fileUpload")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("body", "minimum"))
    }

    @Test
    fun `body with too short content is rejected (Client)`() {
        runBlocking {
            val response = client.fileUploadRequired(byteArrayOf(66))
            if (response is FileUploadRequiredHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages).containsExactly(listOf("body", "minimum"))
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `body with too short content is rejected (Test-Client)`() {
        testClient.fileUploadRequiredSafe(byteArrayOf(66))
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("body", "minimum"))
            }
    }

    @Test
    fun `body with too short content is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/octet-stream")
            .body(byteArrayOf(66))
            .post("/features/octet/required/fileUpload")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("body", "minimum"))
    }

    @Test
    fun `body with content is supported (Client)`() {
        val content = byteArrayOf(66, 67)
        runBlocking {
            val response = client.fileUploadRequired(content)
            if (response is FileUploadRequiredHttpResponse.Ok) {
                assertThat(response.safeBody).isEqualTo(content)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `body with content is supported (Test-Client)`() {
        val content = byteArrayOf(66, 67)
        testClient.fileUploadRequiredSafe(content)
            .isOkResponse {
                assertThat(safeBody).isEqualTo(content)
            }
    }

    @Test
    fun `body with content is supported (Raw)`() {
        val content = byteArrayOf(66, 67)
        val body = prepareRequest()
            .contentType("application/octet-stream")
            .body(content)
            .post("/features/octet/required/fileUpload")
            .execute()
            .statusCode(200)
            .extract()
            .asByteArray()
        assertThat(body).isEqualTo(content)
    }

}