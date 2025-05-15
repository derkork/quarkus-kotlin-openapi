package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.DefaultBodyValuesHttpResponse
import com.ancientlightstudios.example.features.client.DefaultParameterValuesHttpResponse
import com.ancientlightstudios.example.features.client.FeaturesDefaultValueClient
import com.ancientlightstudios.example.features.client.model.DefaultBodyValuesBody
import com.ancientlightstudios.example.features.testclient.FeaturesDefaultValueTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.math.BigDecimal
import java.math.BigInteger
import com.ancientlightstudios.example.features.testclient.model.DefaultBodyValuesBody as TestDefaultBodyValuesBody

@QuarkusTest
class FeaturesDefaultValueTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesDefaultValueClient

    val testClient: FeaturesDefaultValueTestClient
        get() = FeaturesDefaultValueTestClient(dependencyContainer) { prepareRequest() }

    @Test
    fun `default parameter values are used if noting else is specified (Client)`() {
        runBlocking {
            val response = client.defaultParameterValues()
            if (response is DefaultParameterValuesHttpResponse.Ok) {
                assertThat(response.safeBody.stringParam).isEqualTo("foo")
                assertThat(response.safeBody.booleanParam).isEqualTo(true)
                assertThat(response.safeBody.intParam).isEqualTo(10)
                assertThat(response.safeBody.uintParam).isEqualTo(11u)
                assertThat(response.safeBody.longParam).isEqualTo(12L)
                assertThat(response.safeBody.ulongParam).isEqualTo(13uL)
                assertThat(response.safeBody.floatParam).isEqualTo(14.5f)
                assertThat(response.safeBody.doubleParam).isEqualTo(15.5)
                assertThat(response.safeBody.bigDecimalParam).isEqualTo(BigDecimal("16.5"))
                assertThat(response.safeBody.bigIntegerParam).isEqualTo(BigInteger("17"))
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `default parameter values are used if noting else is specified (Test-Client)`() {
        testClient.defaultParameterValuesSafe()
            .isOkResponse {
                assertThat(safeBody.stringParam).isEqualTo("foo")
                assertThat(safeBody.booleanParam).isEqualTo(true)
                assertThat(safeBody.intParam).isEqualTo(10)
                assertThat(safeBody.uintParam).isEqualTo(11u)
                assertThat(safeBody.longParam).isEqualTo(12L)
                assertThat(safeBody.ulongParam).isEqualTo(13uL)
                assertThat(safeBody.floatParam).isEqualTo(14.5f)
                assertThat(safeBody.doubleParam).isEqualTo(15.5)
                assertThat(safeBody.bigDecimalParam).isEqualTo(BigDecimal("16.5"))
                assertThat(safeBody.bigIntegerParam).isEqualTo(BigInteger("17"))
            }
    }

    @Test
    fun `default parameter values are used if noting else is specified (Raw)`() {
        prepareRequest()
            .get("/features/default/defaultParameterValues")
            .execute()
            .statusCode(200)
            .body("stringParam", equalTo("foo"))
            .body("booleanParam", equalTo(true))
            .body("intParam", equalTo(10))
            .body("uintParam", equalTo(11))
            .body("longParam", equalTo(12))
            .body("ulongParam", equalTo(13))
            .body("floatParam", equalTo(14.5f))
            .body("doubleParam", equalTo(15.5f))
            .body("bigDecimalParam", equalTo(16.5f))
            .body("bigIntegerParam", equalTo(17))
    }

    @Test
    fun `default parameter values can be overwritten (Client)`() {
        runBlocking {
            val response = client.defaultParameterValues(
                "bar",
                false,
                20,
                21u,
                22L,
                23uL,
                24.5f,
                25.5,
                BigDecimal("26.5"),
                BigInteger("27")
            )
            if (response is DefaultParameterValuesHttpResponse.Ok) {
                assertThat(response.safeBody.stringParam).isEqualTo("bar")
                assertThat(response.safeBody.booleanParam).isEqualTo(false)
                assertThat(response.safeBody.intParam).isEqualTo(20)
                assertThat(response.safeBody.uintParam).isEqualTo(21u)
                assertThat(response.safeBody.longParam).isEqualTo(22L)
                assertThat(response.safeBody.ulongParam).isEqualTo(23uL)
                assertThat(response.safeBody.floatParam).isEqualTo(24.5f)
                assertThat(response.safeBody.doubleParam).isEqualTo(25.5)
                assertThat(response.safeBody.bigDecimalParam).isEqualTo(BigDecimal("26.5"))
                assertThat(response.safeBody.bigIntegerParam).isEqualTo(BigInteger("27"))
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `default parameter values can be overwritten (Test-Client)`() {
        testClient.defaultParameterValuesSafe(
            "bar",
            false,
            20,
            21u,
            22L,
            23uL,
            24.5f,
            25.5,
            BigDecimal("26.5"),
            BigInteger("27")
        )
            .isOkResponse {
                assertThat(safeBody.stringParam).isEqualTo("bar")
                assertThat(safeBody.booleanParam).isEqualTo(false)
                assertThat(safeBody.intParam).isEqualTo(20)
                assertThat(safeBody.uintParam).isEqualTo(21u)
                assertThat(safeBody.longParam).isEqualTo(22L)
                assertThat(safeBody.ulongParam).isEqualTo(23uL)
                assertThat(safeBody.floatParam).isEqualTo(24.5f)
                assertThat(safeBody.doubleParam).isEqualTo(25.5)
                assertThat(safeBody.bigDecimalParam).isEqualTo(BigDecimal("26.5"))
                assertThat(safeBody.bigIntegerParam).isEqualTo(BigInteger("27"))
            }
    }

    @Test
    fun `default parameter values can be overwritten (Raw)`() {
        prepareRequest()
            .queryParam("stringParam", "bar")
            .queryParam("booleanParam", false)
            .queryParam("intParam", 20)
            .queryParam("uintParam", 21u)
            .queryParam("longParam", 22L)
            .queryParam("ulongParam", 23uL)
            .queryParam("floatParam", 24.5f)
            .queryParam("doubleParam", 25.5)
            .queryParam("bigDecimalParam", BigDecimal("26.5"))
            .queryParam("bigIntegerParam", BigInteger("27"))
            .get("/features/default/defaultParameterValues")
            .execute()
            .statusCode(200)
            .body("stringParam", equalTo("bar"))
            .body("booleanParam", equalTo(false))
            .body("intParam", equalTo(20))
            .body("uintParam", equalTo(21))
            .body("longParam", equalTo(22))
            .body("ulongParam", equalTo(23))
            .body("floatParam", equalTo(24.5f))
            .body("doubleParam", equalTo(25.5f))
            .body("bigDecimalParam", equalTo(26.5f))
            .body("bigIntegerParam", equalTo(27))
    }

    @Test
    fun `default body values are used if noting else is specified (Client)`() {
        runBlocking {
            val body = DefaultBodyValuesBody()
            val response = client.defaultBodyValues(body)
            if (response is DefaultBodyValuesHttpResponse.Ok) {
                assertThat(response.safeBody).usingRecursiveComparison().isEqualTo(body)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `default body values are used if noting else is specified (Test-Client)`() {
        val body = TestDefaultBodyValuesBody()
        testClient.defaultBodyValuesSafe(body)
            .isOkResponse {
                assertThat(safeBody).usingRecursiveComparison().isEqualTo(body)
            }
    }

    @Test
    fun `default body values are used if noting else is specified (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body("{}")
            .post("/features/default/defaultBodyValues")
            .execute()
            .statusCode(200)
            .body("stringParam", equalTo("foo"))
            .body("booleanParam", equalTo(true))
            .body("intParam", equalTo(10))
            .body("uintParam", equalTo(11))
            .body("longParam", equalTo(12))
            .body("ulongParam", equalTo(13))
            .body("floatParam", equalTo(14.5f))
            .body("doubleParam", equalTo(15.5f))
            .body("bigDecimalParam", equalTo(16.5f))
            .body("bigIntegerParam", equalTo(17))
    }

    @Test
    fun `default body values can be overwritten (Client)`() {
        runBlocking {
            val body = DefaultBodyValuesBody(
                "bar",
                false,
                20,
                21u,
                22L,
                23uL,
                24.5f,
                25.5,
                BigDecimal("26.5"),
                BigInteger("27")
            )

            val response = client.defaultBodyValues(body)
            if (response is DefaultBodyValuesHttpResponse.Ok) {
                assertThat(response.safeBody).usingRecursiveComparison().isEqualTo(body)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `default body values can be overwritten (Test-Client)`() {
        val body = TestDefaultBodyValuesBody(
            "bar",
            false,
            20,
            21u,
            22L,
            23uL,
            24.5f,
            25.5,
            BigDecimal("26.5"),
            BigInteger("27")
        )
        testClient.defaultBodyValuesSafe(body)
            .isOkResponse {
                assertThat(safeBody).usingRecursiveComparison().isEqualTo(body)
            }
    }

    @Test
    fun `default body values can be overwritten (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body("""{
                    "stringParam": "bar",
                    "booleanParam": false,
                    "intParam": 20,
                    "uintParam": 21,
                    "longParam": 22,
                    "ulongParam": 23,
                    "floatParam": 24.5,
                    "doubleParam": 25.5,
                    "bigDecimalParam": 26.5,
                    "bigIntegerParam": 27                
                }""".trimMargin())
            .post("/features/default/defaultBodyValues")
            .execute()
            .statusCode(200)
            .body("stringParam", equalTo("bar"))
            .body("booleanParam", equalTo(false))
            .body("intParam", equalTo(20))
            .body("uintParam", equalTo(21))
            .body("longParam", equalTo(22))
            .body("ulongParam", equalTo(23))
            .body("floatParam", equalTo(24.5f))
            .body("doubleParam", equalTo(25.5f))
            .body("bigDecimalParam", equalTo(26.5f))
            .body("bigIntegerParam", equalTo(27))
    }

}