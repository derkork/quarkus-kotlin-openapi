package com.ancientlightstudios.example.features

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.common.http.TestHTTPResource
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeEach
import java.net.URL

abstract class ApiTestBase {

    @TestHTTPResource("")
    var internalTestUrl: URL? = null

    @Inject
    lateinit var objectMapper: ObjectMapper

    private val testUrl: URL
        get() = internalTestUrl
            ?: throw IllegalStateException("testUrl is null. Did you forget to annotate the test class with @QuarkusTest?")

    @BeforeEach
    fun setup() {
    }

    protected fun prepareRequest(): RequestSpecification {
        return RestAssured.given()
            .baseUri(testUrl.toString())
            .log().ifValidationFails()
            .config(
                RestAssuredConfig.config()
                    .httpClient(
                        HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", 10)
                    )
            )
    }
    
    protected fun Response.execute() =
        this.then()
            .log().ifValidationFails()

}