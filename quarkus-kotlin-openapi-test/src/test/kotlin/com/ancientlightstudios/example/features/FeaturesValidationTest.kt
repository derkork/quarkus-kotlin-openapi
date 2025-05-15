package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.*
import com.ancientlightstudios.example.features.client.model.PropertiesOnNestedMapValidationBody
import com.ancientlightstudios.example.features.client.model.PropertiesOnObjectValidationBody
import com.ancientlightstudios.example.features.client.model.PropertiesOnObjectWithDefaultValidationBody
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
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.test.assertTrue
import com.ancientlightstudios.example.features.testclient.ResponseValidationError as TestResponseValidationError
import com.ancientlightstudios.example.features.testclient.model.PropertiesOnNestedMapValidationBody as TestPropertiesOnNestedMapValidationBody
import com.ancientlightstudios.example.features.testclient.model.PropertiesOnObjectValidationBody as TestPropertiesOnObjectValidationBody
import com.ancientlightstudios.example.features.testclient.model.PropertiesOnObjectWithDefaultValidationBody as TestPropertiesOnObjectWithDefaultValidationBody

@QuarkusTest
class FeaturesValidationTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesValidationClient

    val testClient: FeaturesValidationTestClient
        get() = FeaturesValidationTestClient(dependencyContainer) { prepareRequest() }

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
    fun `big decimal below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.bigDecimalValidation(BigDecimal("5.4"), BigDecimal("5.5"))
            if (response is BigDecimalValidationHttpResponse.BadRequest) {
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
    fun `big decimal below minimum are rejected (Test-Client)`() {
        testClient.bigDecimalValidationSafe(BigDecimal("5.4"), BigDecimal("5.5"))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.inclusive", "minimum"),
                        listOf("request.query.exclusive", "exclusive minimum")
                    )
            }
    }

    @Test
    fun `big decimal below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("inclusive", "5.4")
            .queryParam("exclusive", "5.5")
            .get("/features/validation/bigDecimal")
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
    fun `big decimal above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.bigDecimalValidation(BigDecimal("10.6"), BigDecimal("10.5"))
            if (response is BigDecimalValidationHttpResponse.BadRequest) {
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
    fun `big decimal above maximum are rejected (Test-Client)`() {
        testClient.bigDecimalValidationSafe(BigDecimal("10.6"), BigDecimal("10.5"))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.inclusive", "maximum"),
                        listOf("request.query.exclusive", "exclusive maximum")
                    )
            }
    }

    @Test
    fun `big decimal above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("inclusive", "10.6")
            .queryParam("exclusive", "10.5")
            .get("/features/validation/bigDecimal")
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
        "5.5,5.51",
        "10.5,10.49"
    )
    fun `valid big decimal are accepted (Client)`(inclusiveParamValue: BigDecimal, exclusiveParamValue: BigDecimal) {
        runBlocking {
            val response = client.bigDecimalValidation(inclusiveParamValue, exclusiveParamValue)
            if (response !is BigDecimalValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "5.5,5.51",
        "10.5,10.49"
    )
    fun `valid big decimal are accepted (Test-Client)`(
        inclusiveParamValue: BigDecimal,
        exclusiveParamValue: BigDecimal
    ) {
        testClient.bigDecimalValidationSafe(inclusiveParamValue, exclusiveParamValue)
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @CsvSource(
        "5.5,5.51",
        "10.5,10.49"
    )
    fun `valid big decimal are accepted (Raw)`(inclusiveParamValue: BigDecimal, exclusiveParamValue: BigDecimal) {
        prepareRequest()
            .queryParam("inclusive", inclusiveParamValue)
            .queryParam("exclusive", exclusiveParamValue)
            .get("/features/validation/bigDecimal")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `big integer below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.bigIntegerValidation(BigInteger("4"), BigInteger("5"))
            if (response is BigIntegerValidationHttpResponse.BadRequest) {
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
    fun `big integer below minimum are rejected (Test-Client)`() {
        testClient.bigIntegerValidationSafe(BigInteger("4"), BigInteger("5"))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.inclusive", "minimum"),
                        listOf("request.query.exclusive", "exclusive minimum")
                    )
            }
    }

    @Test
    fun `big integer below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("inclusive", 4)
            .queryParam("exclusive", 5)
            .get("/features/validation/bigInteger")
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
    fun `big integer above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.bigIntegerValidation(BigInteger("11"), BigInteger("10"))
            if (response is BigIntegerValidationHttpResponse.BadRequest) {
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
    fun `big integer above maximum are rejected (Test-Client)`() {
        testClient.bigIntegerValidationSafe(BigInteger("11"), BigInteger("10"))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.query.inclusive", "maximum"),
                        listOf("request.query.exclusive", "exclusive maximum")
                    )
            }
    }

    @Test
    fun `big integer above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("inclusive", 11)
            .queryParam("exclusive", 10)
            .get("/features/validation/bigInteger")
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
    fun `valid big integer are accepted (Client)`(inclusiveParamValue: BigInteger, exclusiveParamValue: BigInteger) {
        runBlocking {
            val response = client.bigIntegerValidation(inclusiveParamValue, exclusiveParamValue)
            if (response !is BigIntegerValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "5,6",
        "10,9"
    )
    fun `valid big integer are accepted (Test-Client)`(
        inclusiveParamValue: BigInteger,
        exclusiveParamValue: BigInteger
    ) {
        testClient.bigIntegerValidationSafe(inclusiveParamValue, exclusiveParamValue)
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @CsvSource(
        "5,6",
        "10,9"
    )
    fun `valid big integer are accepted (Raw)`(inclusiveParamValue: BigInteger, exclusiveParamValue: BigInteger) {
        prepareRequest()
            .queryParam("inclusive", inclusiveParamValue)
            .queryParam("exclusive", exclusiveParamValue)
            .get("/features/validation/bigInteger")
            .execute()
            .statusCode(204)
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

    @Test
    fun `pure maps below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.propertiesOnPureMapValidation(("key" to "value").repeatAsMap(1))
            if (response is PropertiesOnPureMapValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `pure maps below minimum are rejected (Test-Client)`() {
        testClient.propertiesOnPureMapValidationSafe(("key" to "value").repeatAsMap(1))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            }
    }

    @Test
    fun `pure maps below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(("key" to "value").repeatAsMap(1))
            .post("/features/validation/propertiesOnPureMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "minimum size")
            )
    }

    @Test
    fun `pure maps above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.propertiesOnPureMapValidation(("key" to "value").repeatAsMap(6))
            if (response is PropertiesOnPureMapValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `pure maps above maximum are rejected (Test-Client)`() {
        testClient.propertiesOnPureMapValidationSafe(("key" to "value").repeatAsMap(6))
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            }
    }

    @Test
    fun `pure maps above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(("key" to "value").repeatAsMap(6))
            .post("/features/validation/propertiesOnPureMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "maximum size")
            )
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 5])
    fun `valid pure maps are accepted (Client)`(length: Int) {
        runBlocking {
            val response = client.propertiesOnPureMapValidation(("key" to "value").repeatAsMap(length))
            if (response !is PropertiesOnPureMapValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 5])
    fun `valid pure maps are accepted (Test-Client)`(length: Int) {
        testClient.propertiesOnPureMapValidationSafe(("key" to "value").repeatAsMap(length))
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 5])
    fun `valid pure maps are accepted (Raw)`(length: Int) {
        prepareRequest()
            .contentType("application/json")
            .body(("key" to "value").repeatAsMap(length))
            .post("/features/validation/propertiesOnPureMap")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `objects with nested maps below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.propertiesOnNestedMapValidation(
                PropertiesOnNestedMapValidationBody("foo", "bar")
            )
            if (response is PropertiesOnNestedMapValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `objects with nested maps below minimum are rejected 2 (Client)`() {
        runBlocking {
            val response = client.propertiesOnNestedMapValidation(
                PropertiesOnNestedMapValidationBody("foo", null, ("key" to "value").repeatAsMap(1))
            )
            if (response is PropertiesOnNestedMapValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `objects with nested maps below minimum are rejected (Test-Client)`() {
        testClient.propertiesOnNestedMapValidationSafe(
            TestPropertiesOnNestedMapValidationBody("foo", "bar")
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            }
    }

    @Test
    fun `objects with nested maps below minimum are rejected 2 (Test-Client)`() {
        testClient.propertiesOnNestedMapValidationSafe(
            TestPropertiesOnNestedMapValidationBody("foo", null, ("key" to "value").repeatAsMap(1))
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            }
    }

    @Test
    fun `objects with nested maps below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo", "bar" to "bar"))
            .post("/features/validation/propertiesOnNestedMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "minimum size")
            )
    }

    @Test
    fun `objects with nested maps below minimum are rejected 2 (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo").plus(("key" to "value").repeatAsMap(1)))
            .post("/features/validation/propertiesOnNestedMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "minimum size")
            )
    }

    @Test
    fun `objects with nested maps above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.propertiesOnNestedMapValidation(
                PropertiesOnNestedMapValidationBody("foo", "bar", ("key" to "value").repeatAsMap(4))
            )
            if (response is PropertiesOnNestedMapValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `objects with nested maps above maximum are rejected 2 (Client)`() {
        runBlocking {
            val response = client.propertiesOnNestedMapValidation(
                PropertiesOnNestedMapValidationBody("foo", null, ("key" to "value").repeatAsMap(5))
            )
            if (response is PropertiesOnNestedMapValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `objects with nested maps above maximum are rejected (Test-Client)`() {
        testClient.propertiesOnNestedMapValidationSafe(
            TestPropertiesOnNestedMapValidationBody("foo", "bar", ("key" to "value").repeatAsMap(4))
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            }
    }

    @Test
    fun `objects with nested maps above maximum are rejected 2 (Test-Client)`() {
        testClient.propertiesOnNestedMapValidationSafe(
            TestPropertiesOnNestedMapValidationBody("foo", null, ("key" to "value").repeatAsMap(5))
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            }
    }

    @Test
    fun `objects with nested maps above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo", "bar" to "bar").plus(("key" to "value").repeatAsMap(4)))
            .post("/features/validation/propertiesOnNestedMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "maximum size")
            )
    }

    @Test
    fun `objects with nested maps above maximum are rejected 2 (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo").plus(("key" to "value").repeatAsMap(5)))
            .post("/features/validation/propertiesOnNestedMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "maximum size")
            )
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 4])
    fun `valid objects with nested maps are accepted (Client)`(length: Int) {
        runBlocking {
            val response = client.propertiesOnNestedMapValidation(
                PropertiesOnNestedMapValidationBody("foo", null, ("key" to "value").repeatAsMap(length))
            )
            if (response !is PropertiesOnNestedMapValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 3])
    fun `valid objects with nested maps are accepted 2 (Client)`(length: Int) {
        runBlocking {
            val response = client.propertiesOnNestedMapValidation(
                PropertiesOnNestedMapValidationBody("foo", "bar", ("key" to "value").repeatAsMap(length))
            )
            if (response !is PropertiesOnNestedMapValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 4])
    fun `valid objects with nested maps are accepted (Test-Client)`(length: Int) {
        testClient.propertiesOnNestedMapValidationSafe(
            TestPropertiesOnNestedMapValidationBody("foo", null, ("key" to "value").repeatAsMap(length))
        )
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 3])
    fun `valid objects with nested maps are accepted 2 (Test-Client)`(length: Int) {
        testClient.propertiesOnNestedMapValidationSafe(
            TestPropertiesOnNestedMapValidationBody("foo", "bar", ("key" to "value").repeatAsMap(length))
        )
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 4])
    fun `valid objects with nested maps are accepted (Raw)`(length: Int) {
        prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo").plus(("key" to "value").repeatAsMap(length)))
            .post("/features/validation/propertiesOnNestedMap")
            .execute()
            .statusCode(204)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 3])
    fun `valid objects with nested maps are accepted 2 (Raw)`(length: Int) {
        prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo", "bar" to "bar").plus(("key" to "value").repeatAsMap(length)))
            .post("/features/validation/propertiesOnNestedMap")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `objects with properties below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.propertiesOnObjectValidation(
                PropertiesOnObjectValidationBody("foo")
            )
            if (response is PropertiesOnObjectValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `objects with properties below minimum are rejected (Test-Client)`() {
        testClient.propertiesOnObjectValidationSafe(
            TestPropertiesOnObjectValidationBody("foo")
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            }
    }

    @Test
    fun `objects with properties below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo"))
            .post("/features/validation/propertiesOnObject")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "minimum size")
            )
    }

    @Test
    fun `objects with properties above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.propertiesOnObjectValidation(
                PropertiesOnObjectValidationBody("foo", "bar", "zort")
            )
            if (response is PropertiesOnObjectValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `objects with properties above maximum are rejected (Test-Client)`() {
        testClient.propertiesOnObjectValidationSafe(
            TestPropertiesOnObjectValidationBody("foo", "bar", "zort")
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            }
    }

    @Test
    fun `objects with properties above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo", "bar" to "bar", "zort" to "zort"))
            .post("/features/validation/propertiesOnObject")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "maximum size")
            )
    }

    @ParameterizedTest
    @CsvSource(
        "bar,",
        ",zort"
    )
    fun `valid objects with properties are accepted (Client)`(bar: String?, zort: String?) {
        runBlocking {
            val response = client.propertiesOnObjectValidation(
                PropertiesOnObjectValidationBody("foo", bar, zort)
            )
            if (response !is PropertiesOnObjectValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "bar,",
        ",zort"
    )
    fun `valid objects with properties are accepted (Test-Client)`(bar: String?, zort: String?) {
        testClient.propertiesOnObjectValidationSafe(
            TestPropertiesOnObjectValidationBody("foo", bar, zort)
        )
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @CsvSource(
        "bar,",
        ",zort"
    )
    fun `valid objects with properties are accepted (Raw)`(bar: String?, zort: String?) {
        prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo", "bar" to bar, "zort" to zort).filterValues { it != null })
            .post("/features/validation/propertiesOnObject")
            .execute()
            .statusCode(204)
    }

    @Test
    fun `objects with properties and defaults below minimum are rejected (Client)`() {
        runBlocking {
            val response = client.propertiesOnObjectWithDefaultValidation(
                PropertiesOnObjectWithDefaultValidationBody()
            )
            if (response is PropertiesOnObjectWithDefaultValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `objects with properties and defaults below minimum are rejected (Test-Client)`() {
        testClient.propertiesOnObjectWithDefaultValidationSafe(
            TestPropertiesOnObjectWithDefaultValidationBody()
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "minimum size")
                    )
            }
    }

    @Test
    fun `objects with properties and defaults below minimum are rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo"))
            .post("/features/validation/propertiesOnObjectWithDefault")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "minimum size")
            )
    }

    @Test
    fun `objects with properties and defaults below minimum are rejected 2 (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf<String, String>())
            .post("/features/validation/propertiesOnObjectWithDefault")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "minimum size")
            )
    }

    @Test
    fun `objects with properties and defaults above maximum are rejected (Client)`() {
        runBlocking {
            val response = client.propertiesOnObjectWithDefaultValidation(
                PropertiesOnObjectWithDefaultValidationBody(bar = "bar", zort = "zort")
            )
            if (response is PropertiesOnObjectWithDefaultValidationHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `objects with properties and defaults above maximum are rejected (Test-Client)`() {
        testClient.propertiesOnObjectWithDefaultValidationSafe(
            TestPropertiesOnObjectWithDefaultValidationBody(bar = "bar", zort = "zort")
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages)
                    .containsExactly(
                        listOf("request.body", "maximum size")
                    )
            }
    }

    @Test
    fun `objects with properties and defaults above maximum are rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("bar" to "bar", "zort" to "zort"))
            .post("/features/validation/propertiesOnObjectWithDefault")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "maximum size")
            )
    }

    @Test
    fun `objects with properties and defaults above maximum are rejected 2 (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo", "bar" to "bar", "zort" to "zort"))
            .post("/features/validation/propertiesOnObjectWithDefault")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages)
            .containsExactly(
                listOf("request.body", "maximum size")
            )
    }

    @ParameterizedTest
    @CsvSource(
        "bar,",
        ",zort"
    )
    fun `valid objects with properties and defaults are accepted (Client)`(bar: String?, zort: String?) {
        runBlocking {
            val response = client.propertiesOnObjectWithDefaultValidation(
                PropertiesOnObjectWithDefaultValidationBody(bar = bar, zort = zort)
            )
            if (response !is PropertiesOnObjectWithDefaultValidationHttpResponse.NoContent) {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "bar,",
        ",zort"
    )
    fun `valid objects with properties and defaults are accepted (Test-Client)`(bar: String?, zort: String?) {
        testClient.propertiesOnObjectWithDefaultValidationSafe(
            TestPropertiesOnObjectWithDefaultValidationBody(bar = bar, zort = zort)
        )
            .isNoContentResponse {
            }
    }

    @ParameterizedTest
    @CsvSource(
        "bar,",
        ",zort"
    )
    fun `valid objects with properties and defaults are accepted (Raw)`(bar: String?, zort: String?) {
        prepareRequest()
            .contentType("application/json")
            .body(mapOf("bar" to bar, "zort" to zort).filterValues { it != null })
            .post("/features/validation/propertiesOnObjectWithDefault")
            .execute()
            .statusCode(204)
    }

    @ParameterizedTest
    @CsvSource(
        "bar,",
        ",zort"
    )
    fun `valid objects with properties and defaults are accepted 2 (Raw)`(bar: String?, zort: String?) {
        prepareRequest()
            .contentType("application/json")
            .body(mapOf("foo" to "foo", "bar" to bar, "zort" to zort).filterValues { it != null })
            .post("/features/validation/propertiesOnObjectWithDefault")
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


