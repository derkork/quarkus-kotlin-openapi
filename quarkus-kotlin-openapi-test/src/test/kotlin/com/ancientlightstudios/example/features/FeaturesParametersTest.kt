package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.*
import com.ancientlightstudios.example.features.testclient.FeaturesParametersTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@QuarkusTest
class FeaturesParametersTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesParametersClient

    val testClient: FeaturesParametersTestClient
        get() = FeaturesParametersTestClient(objectMapper) { prepareRequest() }

    @Test
    fun `path parameters work (Client)`() {
        runBlocking {
            val response = client.parametersPath("foo", 17)
            if (response is ParametersPathHttpResponse.Ok) {
                assertThat(response.safeBody.name).isEqualTo("foo")
                assertThat(response.safeBody.id).isEqualTo(17)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `path parameters work (Test-Client)`() {
        testClient.parametersPathSafe("foo", 17)
            .isOkResponse {
                assertThat(safeBody.name).isEqualTo("foo")
                assertThat(safeBody.id).isEqualTo(17)
            }
    }

    @Test
    fun `path parameters work (Raw)`() {
        prepareRequest()
            .get("/features/parameters/{name}/kind/{id}", mapOf("name" to "foo", "id" to 17))
            .then()
            .statusCode(200)
            .body("name", equalTo("foo"))
            .body("id", equalTo(17))
    }

    @Test
    fun `sending wrong parameter value is rejected (Test-Client)`() {
        testClient.parametersPathRaw("foo", "not-a-number") { this }
            .isBadRequestResponse {}
    }

    @Test
    fun `sending wrong parameter value is rejected (Raw)`() {
        prepareRequest()
            .get("/features/parameters/{name}/kind/{id}", mapOf("name" to "foo", "id" to "not-a-number"))
            .then()
            .statusCode(400)
    }

    @Test
    fun `missing required parameters are rejected (Test-Client)`() {
        testClient.parametersRequiredNotNullUnsafe {}
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(
                    listOf("request.query.querySingleValue", "required"),
                    listOf("request.header.headerSingleValue", "required"),
                    listOf("request.cookie.cookieSingleValue", "required")
                )
            }
    }

    @Test
    fun `missing required parameters are rejected (Raw)`() {
        val messages = prepareRequest()
            .get("/features/parameters/requiredNotNull")
            .then()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.query.querySingleValue", "required"),
            listOf("request.header.headerSingleValue", "required"),
            listOf("request.cookie.cookieSingleValue", "required")
        )
    }

    @Test
    fun `required non null parameters work (Client)`() {
        runBlocking {
            val response = client.parametersRequiredNotNull(
                "queryParam",
                listOf("queryParamItem1", "queryParamItem2"),
                "headerParam",
                listOf("headerParamItem1", "headerParamItem2"),
                "cookieParam"
            )
            if (response is ParametersRequiredNotNullHttpResponse.Ok) {
                assertThat(response.headerSingleValue).isEqualTo("headerParam")
                assertThat(response.headerCollectionValue)
                    .containsExactly("headerParamItem1", "headerParamItem2")
                assertThat(response.safeBody.querySingleValue).isEqualTo("queryParam")
                assertThat(response.safeBody.queryCollectionValue)
                    .containsExactly("queryParamItem1", "queryParamItem2")
                assertThat(response.safeBody.headerSingleValue).isEqualTo("headerParam")
                assertThat(response.safeBody.headerCollectionValue)
                    .containsExactly("headerParamItem1", "headerParamItem2")
                assertThat(response.safeBody.cookieSingleValue).isEqualTo("cookieParam")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `required non null parameters work (Test-Client)`() {
        testClient.parametersRequiredNotNullSafe(
            "queryParam",
            listOf("queryParamItem1", "queryParamItem2"),
            "headerParam",
            listOf("headerParamItem1", "headerParamItem2"),
            "cookieParam"
        ).isOkResponse {
            assertThat(headerSingleValue).isEqualTo("headerParam")
            assertThat(headerCollectionValue)
                .containsExactly("headerParamItem1", "headerParamItem2")
            assertThat(safeBody.querySingleValue).isEqualTo("queryParam")
            assertThat(safeBody.queryCollectionValue)
                .containsExactly("queryParamItem1", "queryParamItem2")
            assertThat(safeBody.headerSingleValue).isEqualTo("headerParam")
            assertThat(safeBody.headerCollectionValue)
                .containsExactly("headerParamItem1", "headerParamItem2")
            assertThat(safeBody.cookieSingleValue).isEqualTo("cookieParam")
        }
    }

    @Test
    fun `required non null parameters work (Raw)`() {
        val response = prepareRequest()
            .queryParam("querySingleValue", "queryParam")
            .queryParam("queryCollectionValue", "queryParamItem1", "queryParamItem2")
            .header("headerSingleValue", "headerParam")
            .header("headerCollectionValue", "headerParamItem1", "headerParamItem2")
            .cookie("cookieSingleValue", "cookieParam")
            .get("/features/parameters/requiredNotNull")

        // headers with multiple values must be validated this way
        val headers = response.headers().getList("headerCollectionValue").map { it.value }
        assertThat(headers).containsExactly("headerParamItem1", "headerParamItem2")

        response.then()
            .statusCode(200)
            .header("headerSingleValue", equalTo("headerParam"))
            .body("querySingleValue", equalTo("queryParam"))
            .body("queryCollectionValue", contains("queryParamItem1", "queryParamItem2"))
            .body("headerSingleValue", equalTo("headerParam"))
            .body("headerCollectionValue", contains("headerParamItem1", "headerParamItem2"))
            .body("cookieSingleValue", equalTo("cookieParam"))
    }

    @Test
    fun `required nullable parameters with real values work (Client)`() {
        runBlocking {
            val response = client.parametersRequiredNullable(
                "queryParam",
                listOf("queryParamItem1", "queryParamItem2"),
                "headerParam",
                listOf("headerParamItem1", "headerParamItem2"),
                "cookieParam"
            )

            if (response is ParametersRequiredNullableHttpResponse.Ok) {
                assertThat(response.headerSingleValue).isEqualTo("headerParam")
                assertThat(response.headerCollectionValue)
                    .containsExactly("headerParamItem1", "headerParamItem2")
                assertThat(response.safeBody.querySingleValue).isEqualTo("queryParam")
                assertThat(response.safeBody.queryCollectionValue)
                    .containsExactly("queryParamItem1", "queryParamItem2")
                assertThat(response.safeBody.headerSingleValue).isEqualTo("headerParam")
                assertThat(response.safeBody.headerCollectionValue)
                    .containsExactly("headerParamItem1", "headerParamItem2")
                assertThat(response.safeBody.cookieSingleValue).isEqualTo("cookieParam")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `required nullable parameters with real values work (Test-Client)`() {
        testClient.parametersRequiredNullableSafe(
            "queryParam",
            listOf("queryParamItem1", "queryParamItem2"),
            "headerParam",
            listOf("headerParamItem1", "headerParamItem2"),
            "cookieParam"
        ).isOkResponse {
            assertThat(headerSingleValue).isEqualTo("headerParam")
            assertThat(headerCollectionValue)
                .containsExactly("headerParamItem1", "headerParamItem2")
            assertThat(safeBody.querySingleValue).isEqualTo("queryParam")
            assertThat(safeBody.queryCollectionValue)
                .containsExactly("queryParamItem1", "queryParamItem2")
            assertThat(safeBody.headerSingleValue).isEqualTo("headerParam")
            assertThat(safeBody.headerCollectionValue)
                .containsExactly("headerParamItem1", "headerParamItem2")
            assertThat(safeBody.cookieSingleValue).isEqualTo("cookieParam")
        }
    }

    @Test
    fun `required nullable parameters with real values work (Raw)`() {
        val response = prepareRequest()
            .queryParam("querySingleValue", "queryParam")
            .queryParam("queryCollectionValue", "queryParamItem1", "queryParamItem2")
            .header("headerSingleValue", "headerParam")
            .header("headerCollectionValue", "headerParamItem1", "headerParamItem2")
            .cookie("cookieSingleValue", "cookieParam")
            .get("/features/parameters/requiredNullable")

        // headers with multiple values must be validated this way
        val headers = response.headers().getList("headerCollectionValue").map { it.value }
        assertThat(headers).containsExactly("headerParamItem1", "headerParamItem2")

        response.then()
            .statusCode(200)
            .header("headerSingleValue", equalTo("headerParam"))
            .body("querySingleValue", equalTo("queryParam"))
            .body("queryCollectionValue", contains("queryParamItem1", "queryParamItem2"))
            .body("headerSingleValue", equalTo("headerParam"))
            .body("headerCollectionValue", contains("headerParamItem1", "headerParamItem2"))
            .body("cookieSingleValue", equalTo("cookieParam"))
    }

    @Test
    fun `required nullable parameters with null values work (Client)`() {
        runBlocking {
            val response = client.parametersRequiredNullable()
            if (response is ParametersRequiredNullableHttpResponse.Ok) {
                assertThat(response.headerSingleValue).isNull()
                assertThat(response.headerCollectionValue).isNull()
                assertThat(response.safeBody.querySingleValue).isNull()
                assertThat(response.safeBody.queryCollectionValue).isEmpty()
                assertThat(response.safeBody.headerSingleValue).isNull()
                assertThat(response.safeBody.headerCollectionValue).isEmpty()
                assertThat(response.safeBody.cookieSingleValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `required nullable parameters with null values work (Test-Client)`() {
        testClient.parametersRequiredNullableSafe(null, null, null, null, null)
            .isOkResponse {
                assertThat(headerSingleValue).isNull()
                assertThat(headerCollectionValue).isEmpty() // TODO: this is different
                assertThat(safeBody.querySingleValue).isNull()
                assertThat(safeBody.queryCollectionValue).isEmpty()
                assertThat(safeBody.headerSingleValue).isNull()
                assertThat(safeBody.headerCollectionValue).isEmpty()
                assertThat(safeBody.cookieSingleValue).isNull()
            }
    }

    @Test
    fun `required nullable parameters with null values work (Raw)`() {
        prepareRequest()
            .get("/features/parameters/requiredNullable")
            .then()
            .statusCode(200)
            .header("headerSingleValue", equalTo(null))
            .header("headerCollectionValue", equalTo(null))
            .body("querySingleValue", equalTo(null))
            .body("queryCollectionValue", hasSize<String>(0))
            .body("headerSingleValue", equalTo(null))
            .body("headerCollectionValue", hasSize<String>(0))
            .body("cookieSingleValue", equalTo(null))
    }

    @Test
    fun `optional non null parameters with real values work (Client)`() {
        runBlocking {
            val response = client.parametersOptionalNotNull(
                "queryParam",
                listOf("queryParamItem1", "queryParamItem2"),
                "headerParam",
                listOf("headerParamItem1", "headerParamItem2"),
                "cookieParam"
            )
            if (response is ParametersOptionalNotNullHttpResponse.Ok) {
                assertThat(response.headerSingleValue).isEqualTo("headerParam")
                assertThat(response.headerCollectionValue)
                    .containsExactly("headerParamItem1", "headerParamItem2")
                assertThat(response.safeBody.querySingleValue).isEqualTo("queryParam")
                assertThat(response.safeBody.queryCollectionValue)
                    .containsExactly("queryParamItem1", "queryParamItem2")
                assertThat(response.safeBody.headerSingleValue).isEqualTo("headerParam")
                assertThat(response.safeBody.headerCollectionValue)
                    .containsExactly("headerParamItem1", "headerParamItem2")
                assertThat(response.safeBody.cookieSingleValue).isEqualTo("cookieParam")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `optional non null parameters with real values work (Test-Client)`() {
        testClient.parametersOptionalNotNullSafe(
            "queryParam",
            listOf("queryParamItem1", "queryParamItem2"),
            "headerParam",
            listOf("headerParamItem1", "headerParamItem2"),
            "cookieParam"
        ).isOkResponse {
            assertThat(headerSingleValue).isEqualTo("headerParam")
            assertThat(headerCollectionValue)
                .containsExactly("headerParamItem1", "headerParamItem2")
            assertThat(safeBody.querySingleValue).isEqualTo("queryParam")
            assertThat(safeBody.queryCollectionValue)
                .containsExactly("queryParamItem1", "queryParamItem2")
            assertThat(safeBody.headerSingleValue).isEqualTo("headerParam")
            assertThat(safeBody.headerCollectionValue)
                .containsExactly("headerParamItem1", "headerParamItem2")
            assertThat(safeBody.cookieSingleValue).isEqualTo("cookieParam")
        }
    }

    @Test
    fun `optional non null parameters with real values work (Raw)`() {
        val response = prepareRequest()
            .queryParam("querySingleValue", "queryParam")
            .queryParam("queryCollectionValue", "queryParamItem1", "queryParamItem2")
            .header("headerSingleValue", "headerParam")
            .header("headerCollectionValue", "headerParamItem1", "headerParamItem2")
            .cookie("cookieSingleValue", "cookieParam")
            .get("/features/parameters/optionalNotNull")

        // headers with multiple values must be validated this way
        val headers = response.headers().getList("headerCollectionValue").map { it.value }
        assertThat(headers).containsExactly("headerParamItem1", "headerParamItem2")

        response.then()
            .statusCode(200)
            .header("headerSingleValue", equalTo("headerParam"))
            .body("querySingleValue", equalTo("queryParam"))
            .body("queryCollectionValue", contains("queryParamItem1", "queryParamItem2"))
            .body("headerSingleValue", equalTo("headerParam"))
            .body("headerCollectionValue", contains("headerParamItem1", "headerParamItem2"))
            .body("cookieSingleValue", equalTo("cookieParam"))
    }

    @Test
    fun `optional non null parameters with null values work (Client)`() {
        runBlocking {
            val response = client.parametersOptionalNotNull()
            if (response is ParametersOptionalNotNullHttpResponse.Ok) {
                assertThat(response.headerSingleValue).isNull()
                assertThat(response.headerCollectionValue).isNull()
                assertThat(response.safeBody.querySingleValue).isNull()
                assertThat(response.safeBody.queryCollectionValue).isEmpty()
                assertThat(response.safeBody.headerSingleValue).isNull()
                assertThat(response.safeBody.headerCollectionValue).isEmpty()
                assertThat(response.safeBody.cookieSingleValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `optional non null parameters with null values work (Test-Client)`() {
        testClient.parametersOptionalNotNullSafe(null, null, null, null, null)
            .isOkResponse {
                assertThat(headerSingleValue).isNull()
                assertThat(headerCollectionValue).isEmpty() // TODO: this is different
                assertThat(safeBody.querySingleValue).isNull()
                assertThat(safeBody.queryCollectionValue).isEmpty()
                assertThat(safeBody.headerSingleValue).isNull()
                assertThat(safeBody.headerCollectionValue).isEmpty()
                assertThat(safeBody.cookieSingleValue).isNull()
            }
    }

    @Test
    fun `optional non null parameters with null values work (Raw)`() {
        prepareRequest()
            .get("/features/parameters/optionalNotNull")
            .then()
            .statusCode(200)
            .header("headerSingleValue", equalTo(null))
            .header("headerCollectionValue", equalTo(null))
            .body("querySingleValue", equalTo(null))
            .body("queryCollectionValue", hasSize<String>(0))
            .body("headerSingleValue", equalTo(null))
            .body("headerCollectionValue", hasSize<String>(0))
            .body("cookieSingleValue", equalTo(null))
    }

    @Test
    fun `optional nullable parameters with real values work (Client)`() {
        runBlocking {
            val response = client.parametersOptionalNullable(
                "queryParam",
                listOf("queryParamItem1", "queryParamItem2"),
                "headerParam",
                listOf("headerParamItem1", "headerParamItem2"),
                "cookieParam"
            )
            if (response is ParametersOptionalNullableHttpResponse.Ok) {
                assertThat(response.headerSingleValue).isEqualTo("headerParam")
                assertThat(response.headerCollectionValue)
                    .containsExactly("headerParamItem1", "headerParamItem2")
                assertThat(response.safeBody.querySingleValue).isEqualTo("queryParam")
                assertThat(response.safeBody.queryCollectionValue)
                    .containsExactly("queryParamItem1", "queryParamItem2")
                assertThat(response.safeBody.headerSingleValue).isEqualTo("headerParam")
                assertThat(response.safeBody.headerCollectionValue)
                    .containsExactly("headerParamItem1", "headerParamItem2")
                assertThat(response.safeBody.cookieSingleValue).isEqualTo("cookieParam")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `optional nullable parameters with real values work (Test-Client)`() {
        testClient.parametersOptionalNullableSafe(
            "queryParam",
            listOf("queryParamItem1", "queryParamItem2"),
            "headerParam",
            listOf("headerParamItem1", "headerParamItem2"),
            "cookieParam"
        ).isOkResponse {
            assertThat(headerSingleValue).isEqualTo("headerParam")
            assertThat(headerCollectionValue)
                .containsExactly("headerParamItem1", "headerParamItem2")
            assertThat(safeBody.querySingleValue).isEqualTo("queryParam")
            assertThat(safeBody.queryCollectionValue)
                .containsExactly("queryParamItem1", "queryParamItem2")
            assertThat(safeBody.headerSingleValue).isEqualTo("headerParam")
            assertThat(safeBody.headerCollectionValue)
                .containsExactly("headerParamItem1", "headerParamItem2")
            assertThat(safeBody.cookieSingleValue).isEqualTo("cookieParam")
        }
    }

    @Test
    fun `optional nullable parameters with real values work (Raw)`() {
        val response = prepareRequest()
            .queryParam("querySingleValue", "queryParam")
            .queryParam("queryCollectionValue", "queryParamItem1", "queryParamItem2")
            .header("headerSingleValue", "headerParam")
            .header("headerCollectionValue", "headerParamItem1", "headerParamItem2")
            .cookie("cookieSingleValue", "cookieParam")
            .get("/features/parameters/optionalNullable")

        // headers with multiple values must be validated this way
        val headers = response.headers().getList("headerCollectionValue").map { it.value }
        assertThat(headers).containsExactly("headerParamItem1", "headerParamItem2")

        response.then()
            .statusCode(200)
            .header("headerSingleValue", equalTo("headerParam"))
            .body("querySingleValue", equalTo("queryParam"))
            .body("queryCollectionValue", contains("queryParamItem1", "queryParamItem2"))
            .body("headerSingleValue", equalTo("headerParam"))
            .body("headerCollectionValue", contains("headerParamItem1", "headerParamItem2"))
            .body("cookieSingleValue", equalTo("cookieParam"))
    }

    @Test
    fun `optional nullable parameters with null values work (Client)`() {
        runBlocking {
            val response = client.parametersOptionalNullable()
            if (response is ParametersOptionalNullableHttpResponse.Ok) {
                assertThat(response.headerSingleValue).isNull()
                assertThat(response.headerCollectionValue).isNull()
                assertThat(response.safeBody.querySingleValue).isNull()
                assertThat(response.safeBody.queryCollectionValue).isEmpty()
                assertThat(response.safeBody.headerSingleValue).isNull()
                assertThat(response.safeBody.headerCollectionValue).isEmpty()
                assertThat(response.safeBody.cookieSingleValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `optional nullable parameters with null values work (Test-Client)`() {
        testClient.parametersOptionalNullableSafe(null, null, null, null, null)
            .isOkResponse {
                assertThat(headerSingleValue).isNull()
                assertThat(headerCollectionValue).isEmpty() // TODO: this is different
                assertThat(safeBody.querySingleValue).isNull()
                assertThat(safeBody.queryCollectionValue).isEmpty()
                assertThat(safeBody.headerSingleValue).isNull()
                assertThat(safeBody.headerCollectionValue).isEmpty()
                assertThat(safeBody.cookieSingleValue).isNull()
            }
    }

    @Test
    fun `optional nullable parameters with null values work (Raw)`() {
        prepareRequest()
            .get("/features/parameters/optionalNullable")
            .then()
            .statusCode(200)
            .header("headerSingleValue", equalTo(null))
            .header("headerCollectionValue", equalTo(null))
            .body("querySingleValue", equalTo(null))
            .body("queryCollectionValue", hasSize<String>(0))
            .body("headerSingleValue", equalTo(null))
            .body("headerCollectionValue", hasSize<String>(0))
            .body("cookieSingleValue", equalTo(null))
    }

}