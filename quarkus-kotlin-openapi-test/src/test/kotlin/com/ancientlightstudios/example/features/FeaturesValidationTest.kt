package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.*
import com.ancientlightstudios.example.features.testclient.FeaturesValidationTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertTrue
import com.ancientlightstudios.example.features.testclient.ResponseValidationError as TestResponseValidationError

@QuarkusTest
class FeaturesValidationTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesValidationClient

    val testClient: FeaturesValidationTestClient
        get() = FeaturesValidationTestClient(objectMapper) { prepareRequest() }

    @Test
    fun `omitted optional values are not validated (Client)`() {
        runBlocking {
            val response = client.optionalValue()
            if (response !is OptionalValueHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `omitted optional values are not validated (Test-Client)`() {
        testClient.optionalValueSafe(null)
            .isNoContentResponse { }
    }

    @Test
    fun `omitted optional values are not validated (Raw)`() {
        prepareRequest()
            .get("/features/validation/optional")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `optional values are validated (Client)`() {
        runBlocking {
            val response = client.optionalValue("fo")
            if (response !is OptionalValueHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `optional values are validated (Test-Client)`() {
        testClient.optionalValueSafe("fo")
            .isNoContentResponse { }
    }

    @Test
    fun `optional values are validated (Raw)`() {
        prepareRequest()
            .queryParam("param", "fo")
            .get("/features/validation/optional")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `invalid optional values are rejected (Client)`() {
        runBlocking {
            val response = client.optionalValue("foo")
            if (response is OptionalValueHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(listOf("request.query.param", "match pattern"))
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `invalid optional values are rejected (Test-Client)`() {
        testClient.optionalValueSafe("foo")
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(listOf("request.query.param", "match pattern"))
            }
    }

    @Test
    fun `invalid optional values are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "foo")
            .get("/features/validation/optional")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(listOf("request.query.param", "match pattern"))
    }

    @Test
    fun `nullable values with null are not validated (Client)`() {
        runBlocking {
            val response = client.nullableValue()
            if (response !is NullableValueHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `nullable values with null are not validated (Test-Client)`() {
        testClient.nullableValueSafe(null)
            .isNoContentResponse { }
    }

    @Test
    fun `nullable values with null are not validated (Raw)`() {
        prepareRequest()
            .get("/features/validation/nullable")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `nullable values are validated (Client)`() {
        runBlocking {
            val response = client.nullableValue("fo")
            if (response !is NullableValueHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `nullable values are validated (Test-Client)`() {
        testClient.nullableValueSafe("fo")
            .isNoContentResponse { }
    }

    @Test
    fun `nullable values are validated (Raw)`() {
        prepareRequest()
            .queryParam("param", "fo")
            .get("/features/validation/nullable")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `invalid nullable values are rejected (Client)`() {
        runBlocking {
            val response = client.nullableValue("foo")
            if (response is NullableValueHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(listOf("request.query.param", "match pattern"))
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `invalid nullable values are rejected (Test-Client)`() {
        testClient.nullableValueSafe("foo")
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(listOf("request.query.param", "match pattern"))
            }
    }

    @Test
    fun `invalid nullable values are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "foo")
            .get("/features/validation/nullable")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(listOf("request.query.param", "match pattern"))
    }

    @Test
    fun `numbers below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.numberValidation(4, 5)
            if (response is NumberValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.inclusive", "minimum"),
                        listOf("request.query.exclusive", "exclusive minimum")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `numbers below minimum are rejected (Test-Client)`() {
        testClient.numberValidationSafe(4, 5)
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.inclusive", "minimum"),
                        listOf("request.query.exclusive", "exclusive minimum")
                    )
            }
    }

    @Test
    fun `numbers below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("inclusive", 4)
            .queryParam("exclusive", 5)
            .get("/features/validation/numbers")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.inclusive", "minimum"),
                listOf("request.query.exclusive", "exclusive minimum")
            )
    }

    @Test
    fun `numbers above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.numberValidation(11, 10)
            if (response is NumberValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.inclusive", "maximum"),
                        listOf("request.query.exclusive", "exclusive maximum")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `numbers above maximum are rejected (Test-Client)`() {
        testClient.numberValidationSafe(11, 10)
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.inclusive", "maximum"),
                        listOf("request.query.exclusive", "exclusive maximum")
                    )
            }
    }

    @Test
    fun `numbers above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("inclusive", 11)
            .queryParam("exclusive", 10)
            .get("/features/validation/numbers")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.inclusive", "maximum"),
                listOf("request.query.exclusive", "exclusive maximum")
            )
    }

    @ParameterizedTest
    @CsvSource(
        "5,6",
        "10,9"
    )
    fun `valid numbers are accepted (Client)`(inclusiveParamValue: Int, exclusiveParamValue: Int) {
        runBlocking {
            val response = client.numberValidation(inclusiveParamValue, exclusiveParamValue)
            if (response !is NumberValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "5,6",
        "10,9"
    )
    fun `valid numbers are accepted (Test-Client)`(inclusiveParamValue: Int, exclusiveParamValue: Int) {
        testClient.numberValidationSafe(inclusiveParamValue, exclusiveParamValue)
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @CsvSource(
        "5,6",
        "10,9"
    )
    fun `valid numbers are accepted (Raw)`(inclusiveParamValue: Int, exclusiveParamValue: Int) {
        prepareRequest()
            .queryParam("inclusive", inclusiveParamValue)
            .queryParam("exclusive", exclusiveParamValue)
            .get("/features/validation/numbers")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `strings below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.stringLengthValidation('0'.repeat(4))
            if (response is StringLengthValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.value", "minimum length")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `strings below minimum are rejected (Test-Client)`() {
        testClient.stringLengthValidationSafe('0'.repeat(4))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.value", "minimum length")
                    )
            }
    }

    @Test
    fun `strings below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("value", '0'.repeat(4))
            .get("/features/validation/stringLength")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.value", "minimum length")
            )
    }

    @Test
    fun `strings above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.stringLengthValidation('0'.repeat(11))
            if (response is StringLengthValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.value", "maximum length")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `strings above maximum are rejected (Test-Client)`() {
        testClient.stringLengthValidationSafe('0'.repeat(11))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.value", "maximum length")
                    )
            }
    }

    @Test
    fun `strings above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("value", '0'.repeat(11))
            .get("/features/validation/stringLength")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.value", "maximum length")
            )
    }

    @ParameterizedTest
    @ValueSource(ints = [5, 10])
    fun `valid strings are accepted (Client)`(length: Int) {
        runBlocking {
            val response = client.stringLengthValidation('0'.repeat(length))
            if (response !is StringLengthValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [5, 10])
    fun `valid strings are accepted (Test-Client)`(length: Int) {
        testClient.stringLengthValidationSafe('0'.repeat(length))
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @ValueSource(ints = [5, 10])
    fun `valid strings are accepted (Raw)`(length: Int) {
        prepareRequest()
            .queryParam("value", '0'.repeat(length))
            .get("/features/validation/stringLength")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `strings violating the pattern are rejected (Client)`() {
        runBlocking {
            val response = client.stringPatternValidation("00")
            if (response is StringPatternValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.value", "pattern")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `strings violating the pattern are rejected (Test-Client)`() {
        testClient.stringPatternValidationSafe("00")
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.value", "pattern")
                    )
            }
    }

    @Test
    fun `strings violating the pattern are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("value", "00")
            .get("/features/validation/stringPattern")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.value", "pattern")
            )
    }

    @Test
    fun `strings with valid pattern are accepted (Client)`() {
        runBlocking {
            val response = client.stringPatternValidation("aa")
            if (response !is StringPatternValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `strings with valid pattern are accepted (Test-Client)`() {
        testClient.stringPatternValidationSafe("aa")
            .isNoContentResponse {
            }
    }

    @Test
    fun `strings with valid pattern are accepted (Raw)`() {
        prepareRequest()
            .queryParam("value", "aa")
            .get("/features/validation/stringPattern")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `arrays below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.arrayValidation(8.repeatAsItem(1))
            if (response is ArrayValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.items", "minimum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `arrays below minimum are rejected (Test-Client)`() {
        testClient.arrayValidationSafe(8.repeatAsItem(1))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.items", "minimum size")
                    )
            }
    }

    @Test
    fun `arrays below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("items", 8.repeatAsItem(1))
            .get("/features/validation/array")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.items", "minimum size")
            )
    }

    @Test
    fun `arrays above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.arrayValidation(8.repeatAsItem(6))
            if (response is ArrayValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.items", "maximum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `arrays above maximum are rejected (Test-Client)`() {
        testClient.arrayValidationSafe(8.repeatAsItem(6))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.items", "maximum size")
                    )
            }
    }

    @Test
    fun `arrays above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("items", 8.repeatAsItem(6))
            .get("/features/validation/array")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.items", "maximum size")
            )
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 5])
    fun `valid arrays are accepted (Client)`(length: Int) {
        runBlocking {
            val response = client.arrayValidation(8.repeatAsItem(length))
            if (response !is ArrayValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 5])
    fun `valid arrays are accepted (Test-Client)`(length: Int) {
        testClient.arrayValidationSafe(8.repeatAsItem(length))
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 5])
    fun `valid arrays are accepted (Raw)`(length: Int) {
        prepareRequest()
            .queryParam("items", 8.repeatAsItem(length))
            .get("/features/validation/array")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `array items below minimum and above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.arrayValidation(listOf(7, 4, 11))
            if (response is ArrayValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.items[1]", "minimum"),
                        listOf("request.query.items[2]", "maximum")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `array items below minimum and above maximum are rejected (Test-Client)`() {
        testClient.arrayValidationSafe(listOf(7, 4, 11))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.items[1]", "minimum"),
                        listOf("request.query.items[2]", "maximum")
                    )
            }
    }

    @Test
    fun `array items below minimum and above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("items", listOf(7, 4, 11))
            .get("/features/validation/array")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.items[1]", "minimum"),
                listOf("request.query.items[2]", "maximum")
            )
    }

    @Test
    fun `valid array items are accepted (Client)`() {
        runBlocking {
            val response = client.arrayValidation(listOf(7, 5, 10))
            if (response !is ArrayValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `valid array items are accepted (Test-Client)`() {
        testClient.arrayValidationSafe(listOf(7, 5, 10))
            .isNoContentResponse {
            }
    }

    @Test
    fun `valid array items are accepted (Raw)`() {
        prepareRequest()
            .queryParam("items", listOf(7, 5, 10))
            .get("/features/validation/array")
            .execute()
            .statusCode(204)
    }

    @ParameterizedTest
    @CsvSource(
        "AAAA, minimum length",
        "AAAAA, must contain the letter 'o'",
        "AOAAA, must only be lowercase"
    )
    fun `custom constraints are applied along with other validation (Client)`(value: String, message: String) {
        runBlocking {
            val response = client.constraintsValidation(value)
            if (response is ConstraintsValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.query.value", message)
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "AAAA, minimum length",
        "AAAAA, must contain the letter 'o'",
        "AOAAA, must only be lowercase"
    )
    fun `custom constraints are applied along with other validation (Test-Client)`(value: String, message: String) {
        testClient.constraintsValidationSafe(value)
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.value", message)
                    )
            }
    }

    @ParameterizedTest
    @CsvSource(
        "AAAA, minimum length",
        "AAAAA, must contain the letter 'o'",
        "AOAAA, must only be lowercase"
    )
    fun `custom constraints are applied along with other validation (Raw)`(value: String, message: String) {
        val messages = prepareRequest()
            .queryParam("value", value)
            .get("/features/validation/constraints")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.query.value", message)
            )
    }

    @Test
    fun `valid strings are accepted by the custom constraints (Client)`() {
        runBlocking {
            val response = client.constraintsValidation('o'.repeat(5))
            if (response !is ConstraintsValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `valid strings are accepted by the custom constraints (Test-Client)`() {
        testClient.constraintsValidationSafe('o'.repeat(5))
            .isNoContentResponse {
            }
    }

    @Test
    fun `valid strings are accepted by the custom constraints (Raw)`() {
        prepareRequest()
            .queryParam("value", 'o'.repeat(5))
            .get("/features/validation/constraints")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `invalid response is detected (Client)`() {
        runBlocking {
            val response = client.responseValidation("AOAAA")
            if (response is ResponseValidationError.ResponseError) {
                assertThat(response.reason).contains("must only be lowercase")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `invalid response is detected (Test-Client)`() {
        testClient.responseValidationSafe("AOAAA")
            .responseSatisfies {
                assertTrue(this is TestResponseValidationError.ResponseError)
            }
    }

    @Test
    fun `invalid response is detected only by the client (Raw)`() {
        val response = prepareRequest()
            .queryParam("response", "AOAAA")
            .get("/features/validation/response")
            .execute()
            .statusCode(200)
            .extract()
            .asString()

        assertThat(response).isEqualTo("AOAAA")
    }

}


