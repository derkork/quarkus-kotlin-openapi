package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.*
import com.ancientlightstudios.example.features.client.model.ResponseCodeHint
import com.ancientlightstudios.example.features.testclient.FeaturesGenericTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import com.ancientlightstudios.example.features.testclient.ResponseCodeError as TestResponseCodeError
import com.ancientlightstudios.example.features.testclient.model.ResponseCodeHint as TestResponseCodeHint

@QuarkusTest
class FeaturesGenericTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesGenericClient

    val testClient: FeaturesGenericTestClient
        get() = FeaturesGenericTestClient(objectMapper) { prepareRequest() }

    // status code 422 is unknown to jakarta.ws.rs.core.Response.Status
    @Test
    fun `unknown status codes are supported (Client)`() {
        runBlocking {
            val response = client.unknownStatusCode()
            if (response !is UnknownStatusCodeHttpResponse.Status422) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `unknown status codes are supported (Test-Client)`() {
        testClient.unknownStatusCodeSafe()
            .isStatus422Response { }
    }

    @Test
    fun `unknown status codes are supported (Raw)`() {
        prepareRequest()
            .get("/features/generic/unknownStatusCode")
            .then()
            .statusCode(422)
    }

    @Test
    fun `mapped status codes are supported (Client)`() {
        runBlocking {
            val response = client.responseCode(ResponseCodeHint.Ok)
            if (response !is ResponseCodeHttpResponse.Ok) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `mapped status codes are supported (Test-Client)`() {
        testClient.responseCodeSafe(TestResponseCodeHint.Ok)
            .isOkResponse { }
    }

    @Test
    fun `mapped status codes are supported (Raw)`() {
        prepareRequest()
            .get("/features/generic/responseCode/200")
            .then()
            .statusCode(200)
    }

    @Test
    fun `returns an error for an unmapped status code (Client)`() {
        runBlocking {
            val response = client.responseCode(ResponseCodeHint.NoContent)
            if (response is ResponseCodeError.ResponseError) {
                assertThat(response.reason).isEqualTo("unknown status code 204")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `returns an error for an unmapped status code (Test-Client)`() {
        testClient.responseCodeSafe(TestResponseCodeHint.NoContent)
            .responseSatisfies {
                assertThat(this is TestResponseCodeError.ResponseError)
            }
    }

    @Test
    fun `unmapped status codes via status-method are supported (Raw)`() {
        prepareRequest()
            .get("/features/generic/responseCode/204")
            .then()
            .statusCode(204)
    }

    @Test
    fun `mapped status code works even if a default mapping exists (Client)`() {
        runBlocking {
            val response = client.responseCodeWithDefault(ResponseCodeHint.Ok)
            if (response !is ResponseCodeWithDefaultHttpResponse.Ok) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `mapped status code works even if a default mapping exists (Test-Client)`() {
        testClient.responseCodeWithDefaultSafe(TestResponseCodeHint.Ok)
            .isOkResponse { }
    }

    @Test
    fun `mapped status code works even if a default mapping exists (Raw)`() {
        prepareRequest()
            .get("/features/generic/responseCodeWithDefault/200")
            .then()
            .statusCode(200)
    }

    @Test
    fun `default mapping is used for unmapped status codes (Client)`() {
        runBlocking {
            val response = client.responseCodeWithDefault(ResponseCodeHint.NoContent)
            if (response is ResponseCodeWithDefaultHttpResponse.Default) {
                assertThat(response.status).isEqualTo(204)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `default mapping is used for unmapped status codes (Test-Client)`() {
        testClient.responseCodeWithDefaultSafe(TestResponseCodeHint.NoContent)
            .isDefaultResponse {
                assertThat(status).isEqualTo(204)
            }
    }

    @Test
    fun `default mapping is used for unmapped status codes (Raw)`() {
        prepareRequest()
            .get("/features/generic/responseCodeWithDefault/204")
            .then()
            .statusCode(204)
    }

}