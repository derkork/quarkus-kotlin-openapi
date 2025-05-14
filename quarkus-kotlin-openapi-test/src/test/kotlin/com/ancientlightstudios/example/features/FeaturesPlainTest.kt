package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.*
import com.ancientlightstudios.example.features.client.model.PlainEnum
import com.ancientlightstudios.example.features.testclient.FeaturesPlainTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal
import java.math.BigInteger
import com.ancientlightstudios.example.features.testclient.model.PlainEnum as TestPlainEnum

@QuarkusTest
class FeaturesPlainTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesPlainClient

    val testClient: FeaturesPlainTestClient
        get() = FeaturesPlainTestClient(dependencyContainer) { prepareRequest() }

    @Test
    fun `sending invalid big integer value is rejected (Test-Client)`() {
        testClient.plainBigIntegerTypeRaw {
            queryParam("param", "foo")
                .body("foo")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("request.query.param", "not a valid integer"),
                listOf("request.body", "not a valid integer")
            )
        }
    }

    @Test
    fun `sending invalid big integer value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "foo")
            .contentType("text/plain")
            .body("foo")
            .post("/features/plain/bigIntegerType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.query.param", "not a valid integer"),
            listOf("request.body", "not a valid integer")
        )
    }

    @Test
    fun `sending empty big integer parameter value is rejected (Test-Client)`() {
        testClient.plainBigIntegerTypeRaw {
            queryParam("param", "")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.query.param", "not a valid integer"))
            }
    }

    @Test
    fun `sending empty big integer parameter value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "")
            .contentType("text/plain")
            .post("/features/plain/bigIntegerType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.query.param", "not a valid integer"))
    }

    @Test
    fun `sending valid big integer value is accepted (Client)`() {
        runBlocking {
            val response = client.plainBigIntegerType(BigInteger("5"), BigInteger("10"))
            if (response is PlainBigIntegerTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isEqualTo(BigInteger("5"))
                assertThat(response.safeBody.bodyValue).isEqualTo(BigInteger("10"))
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending valid big integer value is accepted (Test-Client)`() {
        testClient.plainBigIntegerTypeSafe(BigInteger("5"), BigInteger("10"))
            .isOkResponse {
                assertThat(safeBody.parameterValue).isEqualTo(BigInteger("5"))
                assertThat(safeBody.bodyValue).isEqualTo(BigInteger("10"))
            }
    }

    @Test
    fun `sending valid big integer value is accepted (Raw)`() {
        prepareRequest()
            .queryParam("param", 5)
            .contentType("text/plain")
            .body(10)
            .post("/features/plain/bigIntegerType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(5))
            .body("bodyValue", equalTo(10))
    }

    @Test
    fun `sending no big integer value is accepted (Client)`() {
        runBlocking {
            val response = client.plainBigIntegerType(null, null)
            if (response is PlainBigIntegerTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isNull()
                assertThat(response.safeBody.bodyValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending no big integer value is accepted (Test-Client)`() {
        testClient.plainBigIntegerTypeSafe(null, null)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending no big integer value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/plain/bigIntegerType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending empty big integer body value is accepted (Test-Client)`() {
        testClient.plainBigIntegerTypeRaw {
            body("")
        }
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending empty big integer body value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .body("")
            .post("/features/plain/bigIntegerType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending invalid integer value is rejected (Test-Client)`() {
        testClient.plainIntegerTypeRaw {
            queryParam("param", "foo")
                .body("foo")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("request.query.param", "not an int"),
                listOf("request.body", "not an int")
            )
        }
    }

    @Test
    fun `sending invalid integer value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "foo")
            .contentType("text/plain")
            .body("foo")
            .post("/features/plain/integerType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.query.param", "not an int"),
            listOf("request.body", "not an int")
        )
    }

    @Test
    fun `sending empty integer parameter value is rejected (Test-Client)`() {
        testClient.plainIntegerTypeRaw {
            queryParam("param", "")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.query.param", "not an int"))
            }
    }

    @Test
    fun `sending empty integer parameter value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "")
            .contentType("text/plain")
            .post("/features/plain/integerType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.query.param", "not an int"))
    }

    @Test
    fun `sending valid integer value is accepted (Client)`() {
        runBlocking {
            val response = client.plainIntegerType(5, 10)
            if (response is PlainIntegerTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isEqualTo(5)
                assertThat(response.safeBody.bodyValue).isEqualTo(10)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending valid integer value is accepted (Test-Client)`() {
        testClient.plainIntegerTypeSafe(5, 10)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isEqualTo(5)
                assertThat(safeBody.bodyValue).isEqualTo(10)
            }
    }

    @Test
    fun `sending valid integer value is accepted (Raw)`() {
        prepareRequest()
            .queryParam("param", 5)
            .contentType("text/plain")
            .body(10)
            .post("/features/plain/integerType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(5))
            .body("bodyValue", equalTo(10))
    }

    @Test
    fun `sending no integer value is accepted (Client)`() {
        runBlocking {
            val response = client.plainIntegerType(null, null)
            if (response is PlainIntegerTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isNull()
                assertThat(response.safeBody.bodyValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending no integer value is accepted (Test-Client)`() {
        testClient.plainIntegerTypeSafe(null, null)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending no integer value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/plain/integerType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending empty integer body value is accepted (Test-Client)`() {
        testClient.plainIntegerTypeRaw {
            body("")
        }
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending empty integer body value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .body("")
            .post("/features/plain/integerType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending invalid big decimal value is rejected (Test-Client)`() {
        testClient.plainBigDecimalTypeRaw {
            queryParam("param", "foo")
                .body("foo")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("request.query.param", "not a valid decimal"),
                listOf("request.body", "not a valid decimal")
            )
        }
    }

    @Test
    fun `sending invalid big decimal value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "foo")
            .contentType("text/plain")
            .body("foo")
            .post("/features/plain/bigDecimalType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.query.param", "not a valid decimal"),
            listOf("request.body", "not a valid decimal")
        )
    }

    @Test
    fun `sending empty big decimal parameter value is rejected (Test-Client)`() {
        testClient.plainBigDecimalTypeRaw {
            queryParam("param", "")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.query.param", "not a valid decimal"))
            }
    }

    @Test
    fun `sending empty big decimal parameter value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "")
            .contentType("text/plain")
            .post("/features/plain/bigDecimalType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.query.param", "not a valid decimal"))
    }

    @Test
    fun `sending valid big decimal value is accepted (Client)`() {
        runBlocking {
            val response = client.plainBigDecimalType(BigDecimal("5.5"), BigDecimal("10.5"))
            if (response is PlainBigDecimalTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isEqualTo(BigDecimal("5.5"))
                assertThat(response.safeBody.bodyValue).isEqualTo(BigDecimal("10.5"))
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending valid big decimal value is accepted (Test-Client)`() {
        testClient.plainBigDecimalTypeSafe(BigDecimal("5.5"), BigDecimal("10.5"))
            .isOkResponse {
                assertThat(safeBody.parameterValue).isEqualTo(BigDecimal("5.5"))
                assertThat(safeBody.bodyValue).isEqualTo(BigDecimal("10.5"))
            }
    }

    @Test
    fun `sending valid big decimal value is accepted (Raw)`() {
        prepareRequest()
            .queryParam("param", 5.5f)
            .contentType("text/plain")
            .body(10.5f)
            .post("/features/plain/bigDecimalType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(5.5f))
            .body("bodyValue", equalTo(10.5f))
    }

    @Test
    fun `sending no big decimal value is accepted (Client)`() {
        runBlocking {
            val response = client.plainBigDecimalType(null, null)
            if (response is PlainBigDecimalTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isNull()
                assertThat(response.safeBody.bodyValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending no big decimal value is accepted (Test-Client)`() {
        testClient.plainBigDecimalTypeSafe(null, null)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending no big decimal value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/plain/bigDecimalType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending empty big decimal body value is accepted (Test-Client)`() {
        testClient.plainBigDecimalTypeRaw {
            body("")
        }
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending empty big decimal body value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .body("")
            .post("/features/plain/bigDecimalType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending invalid floating value is rejected (Test-Client)`() {
        testClient.plainFloatingTypeRaw {
            queryParam("param", "foo")
                .body("foo")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("request.query.param", "not a float"),
                listOf("request.body", "not a float")
            )
        }
    }

    @Test
    fun `sending invalid floating value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "foo")
            .contentType("text/plain")
            .body("foo")
            .post("/features/plain/floatingType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.query.param", "not a float"),
            listOf("request.body", "not a float")
        )
    }

    @Test
    fun `sending empty floating parameter value is rejected (Test-Client)`() {
        testClient.plainFloatingTypeRaw {
            queryParam("param", "")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.query.param", "not a float"))
            }
    }

    @Test
    fun `sending empty floating parameter value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "")
            .contentType("text/plain")
            .post("/features/plain/floatingType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.query.param", "not a float"))
    }

    @Test
    fun `sending valid floating value is accepted (Client)`() {
        runBlocking {
            val response = client.plainFloatingType(5.5f, 10.5f)
            if (response is PlainFloatingTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isEqualTo(5.5f)
                assertThat(response.safeBody.bodyValue).isEqualTo(10.5f)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending valid floating value is accepted (Test-Client)`() {
        testClient.plainFloatingTypeSafe(5.5f, 10.5f)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isEqualTo(5.5f)
                assertThat(safeBody.bodyValue).isEqualTo(10.5f)
            }
    }

    @Test
    fun `sending valid floating value is accepted (Raw)`() {
        prepareRequest()
            .queryParam("param", 5.5f)
            .contentType("text/plain")
            .body(10.5f)
            .post("/features/plain/floatingType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(5.5f))
            .body("bodyValue", equalTo(10.5f))
    }

    @Test
    fun `sending no floating value is accepted (Client)`() {
        runBlocking {
            val response = client.plainFloatingType(null, null)
            if (response is PlainFloatingTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isNull()
                assertThat(response.safeBody.bodyValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending no floating value is accepted (Test-Client)`() {
        testClient.plainFloatingTypeSafe(null, null)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending no floating value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/plain/floatingType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending empty floating body value is accepted (Test-Client)`() {
        testClient.plainFloatingTypeRaw {
            body("")
        }
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending empty floating body value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .body("")
            .post("/features/plain/floatingType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending invalid boolean value is rejected (Test-Client)`() {
        testClient.plainBooleanTypeRaw {
            queryParam("param", "foo")
                .body("foo")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("request.query.param", "not a boolean"),
                listOf("request.body", "not a boolean")
            )
        }
    }

    @Test
    fun `sending invalid boolean value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "foo")
            .contentType("text/plain")
            .body("foo")
            .post("/features/plain/booleanType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.query.param", "not a boolean"),
            listOf("request.body", "not a boolean")
        )
    }

    @Test
    fun `sending empty boolean parameter value is rejected (Test-Client)`() {
        testClient.plainBooleanTypeRaw {
            queryParam("param", "")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.query.param", "not a boolean"))
            }
    }

    @Test
    fun `sending empty boolean parameter value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "")
            .contentType("text/plain")
            .post("/features/plain/booleanType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.query.param", "not a boolean"))
    }

    @Test
    fun `sending valid boolean value is accepted (Client)`() {
        runBlocking {
            val response = client.plainBooleanType(true, true)
            if (response is PlainBooleanTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isEqualTo(true)
                assertThat(response.safeBody.bodyValue).isEqualTo(true)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending valid boolean value is accepted (Test-Client)`() {
        testClient.plainBooleanTypeSafe(true, true)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isEqualTo(true)
                assertThat(safeBody.bodyValue).isEqualTo(true)
            }
    }

    @Test
    fun `sending valid boolean value is accepted (Raw)`() {
        prepareRequest()
            .queryParam("param", true)
            .contentType("text/plain")
            .body(true)
            .post("/features/plain/booleanType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(true))
            .body("bodyValue", equalTo(true))
    }

    @Test
    fun `sending no boolean value is accepted (Client)`() {
        runBlocking {
            val response = client.plainBooleanType(null, null)
            if (response is PlainBooleanTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isNull()
                assertThat(response.safeBody.bodyValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending no boolean value is accepted (Test-Client)`() {
        testClient.plainBooleanTypeSafe(null, null)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending no boolean value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/plain/booleanType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending empty boolean body value is accepted (Test-Client)`() {
        testClient.plainBooleanTypeRaw {
            body("")
        }
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending empty boolean body value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .body("")
            .post("/features/plain/booleanType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending valid string value is accepted (Client)`() {
        runBlocking {
            val response = client.plainStringType("foo", "bar")
            if (response is PlainStringTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isEqualTo("foo")
                assertThat(response.safeBody.bodyValue).isEqualTo("bar")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending valid string value is accepted (Test-Client)`() {
        testClient.plainStringTypeSafe("foo", "bar")
            .isOkResponse {
                assertThat(safeBody.parameterValue).isEqualTo("foo")
                assertThat(safeBody.bodyValue).isEqualTo("bar")
            }
    }

    @Test
    fun `sending valid string value is accepted (Raw)`() {
        prepareRequest()
            .queryParam("param", "foo")
            .contentType("text/plain")
            .body("bar")
            .post("/features/plain/stringType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo("foo"))
            .body("bodyValue", equalTo("bar"))
    }

    @Test
    fun `sending no string value is accepted (Client)`() {
        runBlocking {
            val response = client.plainStringType(null, null)
            if (response is PlainStringTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isNull()
                assertThat(response.safeBody.bodyValue).isEmpty()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending no string value is accepted (Test-Client)`() {
        testClient.plainStringTypeSafe(null, null)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isEmpty()
            }
    }

    @Test
    fun `sending no string value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/plain/stringType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(""))
    }

    @Test
    fun `sending empty string body value is accepted (Test-Client)`() {
        testClient.plainStringTypeRaw {
            body("")
        }
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isEmpty()
            }
    }

    @Test
    fun `sending empty string body value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .body("")
            .post("/features/plain/stringType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(""))
    }

    @Test
    fun `sending invalid enum value is rejected (Test-Client)`() {
        testClient.plainEnumTypeRaw {
            queryParam("param", "foo")
                .body("foo")
        }.isBadRequestResponse {
            assertThat(safeBody.messages).containsExactly(
                listOf("request.query.param", "not a valid value"),
                listOf("request.body", "not a valid value")
            )
        }
    }

    @Test
    fun `sending invalid enum value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "foo")
            .contentType("text/plain")
            .body("foo")
            .post("/features/plain/enumType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.query.param", "not a valid value"),
            listOf("request.body", "not a valid value")
        )
    }

    @Test
    fun `sending empty enum parameter value is rejected (Test-Client)`() {
        testClient.plainEnumTypeRaw {
            queryParam("param", "")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.query.param", "not a valid value"))
            }
    }

    @Test
    fun `sending empty enum parameter value is rejected (Raw)`() {
        val messages = prepareRequest()
            .queryParam("param", "")
            .contentType("text/plain")
            .post("/features/plain/enumType")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.query.param", "not a valid value"))
    }

    @ParameterizedTest
    @EnumSource(value = PlainEnum::class)
    fun `sending valid enum value is accepted (Client)`(value: PlainEnum) {
        runBlocking {
            val response = client.plainEnumType(value, value)
            if (response is PlainEnumTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isEqualTo(value)
                assertThat(response.safeBody.bodyValue).isEqualTo(value)
            } else {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = TestPlainEnum::class)
    fun `sending valid enum value is accepted (Test-Client)`(value: TestPlainEnum) {
        testClient.plainEnumTypeSafe(value, value)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isEqualTo(value)
                assertThat(safeBody.bodyValue).isEqualTo(value)
            }
    }

    @ParameterizedTest
    @EnumSource(value = PlainEnum::class)
    fun `sending valid enum value is accepted (Raw)`(value: PlainEnum) {
        prepareRequest()
            .queryParam("param", value.value)
            .contentType("text/plain")
            .body(value.value)
            .post("/features/plain/enumType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(value.value))
            .body("bodyValue", equalTo(value.value))
    }

    @Test
    fun `sending no enum value is accepted (Client)`() {
        runBlocking {
            val response = client.plainEnumType(null, null)
            if (response is PlainEnumTypeHttpResponse.Ok) {
                assertThat(response.safeBody.parameterValue).isNull()
                assertThat(response.safeBody.bodyValue).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending no enum value is accepted (Test-Client)`() {
        testClient.plainEnumTypeSafe(null, null)
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending no enum value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .post("/features/plain/enumType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }

    @Test
    fun `sending empty enum body value is accepted (Test-Client)`() {
        testClient.plainEnumTypeRaw {
            body("")
        }
            .isOkResponse {
                assertThat(safeBody.parameterValue).isNull()
                assertThat(safeBody.bodyValue).isNull()
            }
    }

    @Test
    fun `sending empty enum body value is accepted (Raw)`() {
        prepareRequest()
            .contentType("text/plain")
            .body("")
            .post("/features/plain/enumType")
            .execute()
            .statusCode(200)
            .body("parameterValue", equalTo(null))
            .body("bodyValue", equalTo(null))
    }
}