package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.testclient.FeaturesGenericTestClient
import com.ancientlightstudios.example.features.testclient.model.ResponseCodeHint
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.opentest4j.AssertionFailedError

@QuarkusTest
class TestClientTest : ApiTestBase() {

    val client: FeaturesGenericTestClient
        get() = FeaturesGenericTestClient(objectMapper) { prepareRequest() }

    @ParameterizedTest
    @EnumSource(value = ResponseCodeHint::class)
    fun `the generic response verification is always available`(statusCode: ResponseCodeHint) {
        var called = false
        client.responseCodeSafe(statusCode)
            .responseSatisfies {
                called = true
            }

        assertThat(called).isTrue()
    }

    @Test
    fun `the generic response verification can fail the test`() {
        assertThrows<AssertionFailedError> {
            client.responseCodeSafe(ResponseCodeHint.Ok)
                .responseSatisfies {
                    assertTrue(false) // assume there is something invalid in the response
                }
        }
    }

    @Test
    fun `if the request is ok, the ok-validator is available`() {
        var called = false
        client.responseCodeSafe(ResponseCodeHint.Ok)
            .isOkResponse {
                called = true
            }

        assertThat(called).isTrue()
    }

    @Test
    fun `if the request is ok, response validations can fail the test`() {
        assertThrows<AssertionFailedError> {
            client.responseCodeSafe(ResponseCodeHint.Ok)
                .isOkResponse {
                    assertTrue(false) // assume there is something invalid in the response
                }
        }
    }

    @Test
    fun `if the request is ok, the badRequest-validator is not available`() {
        assertThrows<AssertionFailedError> {
            client.responseCodeSafe(ResponseCodeHint.Ok)
                .isBadRequestResponse {}
        }
    }

    @Test
    fun `if the request is not ok, the ok-validator is not available`() {
        assertThrows<AssertionFailedError> {
            client.responseCodeSafe(ResponseCodeHint.BadRequest)
                .isOkResponse {}
        }
    }

    @Test
    fun `if the request is not ok, the badRequest-validator is available`() {
        var called = false
        client.responseCodeSafe(ResponseCodeHint.BadRequest)
            .isBadRequestResponse {
                called = true
            }
        assertThat(called).isTrue()
    }


}