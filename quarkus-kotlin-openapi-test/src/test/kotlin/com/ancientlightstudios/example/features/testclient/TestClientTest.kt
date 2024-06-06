package com.ancientlightstudios.example.features.testclient

import com.ancientlightstudios.example.features.ApiTestBase
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

@QuarkusTest
class TestClientTest : ApiTestBase() {

    val client: FeaturesParametersTestClient
        get() = FeaturesParametersTestClient(objectMapper) { prepareRequest() }

    @Test
    fun `if the request is ok, the ok-validator is available`() {
        var called = false
        client.parametersRequiredNotNullSafe("foo", emptyList(), "narf", emptyList(), "zort")
            .isOkResponse {
                called = true
            }

        assertThat(called).isTrue()
    }

    @Test
    fun `if the request is ok, response validations can fail the test`() {
        assertThrows<AssertionFailedError> {
            client.parametersRequiredNotNullSafe("foo", emptyList(), "narf", emptyList(), "zort")
                .isOkResponse {
                    assertTrue(false)
                }
        }
    }

    @Test
    fun `if the request is ok, the badRequest-validator is not available`() {
        assertThrows<AssertionFailedError> {
            client.parametersRequiredNotNullSafe("foo", emptyList(), "narf", emptyList(), "zort")
                .isBadRequestResponse {}
        }
    }

    @Test
    fun `if the request is not ok, the ok-validator is not available`() {
        assertThrows<AssertionFailedError> {
            client.parametersRequiredNotNullUnsafe()
                .isOkResponse {}
        }
    }

    @Test
    fun `if the request is not ok, the badRequest-validator is available`() {
        var called = false
        client.parametersRequiredNotNullUnsafe()
            .isBadRequestResponse {
                called = true
            }
        assertThat(called).isTrue()
    }


}