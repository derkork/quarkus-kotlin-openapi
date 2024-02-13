package com.ancientlightstudios.example.features

import io.quarkus.test.common.http.TestHTTPResource
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.response.ValidatableResponse
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.BeforeEach
import java.net.URL

abstract class ApiTestBase {

    @TestHTTPResource("")
    var internalTestUrl: URL? = null

    private val testUrl: URL
        get() = internalTestUrl
            ?: throw IllegalStateException("testUrl is null. Did you forget to annotate the test class with @QuarkusTest?")

    @BeforeEach
    fun setup() {
    }

    fun String.toTestUrl(): String {
        // strip leading slash then add it to test url
        val stripped = this.removePrefix("/")
        return "$testUrl$stripped"
    }

    protected fun prepareRequest(contentType: String = "application/json"): RequestSpecification {
        return RestAssured.given()
            .log().ifValidationFails()
            .config(
                RestAssuredConfig
                    .config()
                    .httpClient(
                        HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", 10)
                    )
            )
            .contentType(contentType)
    }

    protected fun Response.execute(): ValidatableResponse = this.then().log().ifValidationFails()

    protected fun ValidatableResponse.withJsonBody(block: (JsonPath) -> Unit): ValidatableResponse {
        extract()
            .body()
            .jsonPath()
            .also(block)
        return this
    }

    protected fun ValidatableResponse.withStringBody(block: (String) -> Unit): ValidatableResponse {
        extract()
            .body()
            .asString()
            .also(block)
        return this
    }

}