package com.ancientlightstudios.example.features.client

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@QuarkusTest
class FeaturesParametersClientTest {

    @Inject
    lateinit var client: FeaturesParametersClient

    @Test
    fun `path parameters work`() {
        runBlocking {
            when (val response = client.parametersPath("foo", 17)) {
                is ParametersPathHttpResponse.Ok -> {
                    Assertions.assertThat(response.safeBody.name).isEqualTo("foo")
                    Assertions.assertThat(response.safeBody.id).isEqualTo(17)
                }

                is ParametersPathHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `required non null parameters work`() {
        runBlocking {
            when (val response = client.parametersRequiredNotNull(
                "queryParam",
                listOf("queryParamItem1", "queryParamItem2"),
                "headerParam",
                listOf("headerParamItem1", "headerParamItem2"),
                "cookieParam"
            )) {
                is ParametersRequiredNotNullHttpResponse.Ok -> {
                    Assertions.assertThat(response.headerSingleValue).isEqualTo("headerParam")
                    Assertions.assertThat(response.headerCollectionValue)
                        .containsExactly("headerParamItem1", "headerParamItem2")
                    Assertions.assertThat(response.safeBody.querySingleValue).isEqualTo("queryParam")
                    Assertions.assertThat(response.safeBody.queryCollectionValue)
                        .containsExactly("queryParamItem1", "queryParamItem2")
                    Assertions.assertThat(response.safeBody.headerSingleValue).isEqualTo("headerParam")
                    Assertions.assertThat(response.safeBody.headerCollectionValue)
                        .containsExactly("headerParamItem1", "headerParamItem2")
                    Assertions.assertThat(response.safeBody.cookieSingleValue).isEqualTo("cookieParam")
                }

                is ParametersRequiredNotNullHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `required nullable parameters with real values work`() {
        runBlocking {
            when (val response = client.parametersRequiredNullable(
                "queryParam",
                listOf("queryParamItem1", "queryParamItem2"),
                "headerParam",
                listOf("headerParamItem1", "headerParamItem2"),
                "cookieParam"
            )) {
                is ParametersRequiredNullableHttpResponse.Ok -> {
                    Assertions.assertThat(response.headerSingleValue).isEqualTo("headerParam")
                    Assertions.assertThat(response.headerCollectionValue)
                        .containsExactly("headerParamItem1", "headerParamItem2")
                    Assertions.assertThat(response.safeBody.querySingleValue).isEqualTo("queryParam")
                    Assertions.assertThat(response.safeBody.queryCollectionValue)
                        .containsExactly("queryParamItem1", "queryParamItem2")
                    Assertions.assertThat(response.safeBody.headerSingleValue).isEqualTo("headerParam")
                    Assertions.assertThat(response.safeBody.headerCollectionValue)
                        .containsExactly("headerParamItem1", "headerParamItem2")
                    Assertions.assertThat(response.safeBody.cookieSingleValue).isEqualTo("cookieParam")
                }

                is ParametersRequiredNullableHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `required nullable parameters with null values work`() {
        runBlocking {
            when (val response = client.parametersRequiredNullable()) {
                is ParametersRequiredNullableHttpResponse.Ok -> {
                    Assertions.assertThat(response.headerSingleValue).isNull()
                    Assertions.assertThat(response.headerCollectionValue).isNull()
                    Assertions.assertThat(response.safeBody.querySingleValue).isNull()
                    Assertions.assertThat(response.safeBody.queryCollectionValue).isEmpty()
                    Assertions.assertThat(response.safeBody.headerSingleValue).isNull()
                    Assertions.assertThat(response.safeBody.headerCollectionValue).isEmpty()
                    Assertions.assertThat(response.safeBody.cookieSingleValue).isNull()
                }

                is ParametersRequiredNullableHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `optional non null parameters with real values work`() {
        runBlocking {
            when (val response = client.parametersOptionalNotNull(
                "queryParam",
                listOf("queryParamItem1", "queryParamItem2"),
                "headerParam",
                listOf("headerParamItem1", "headerParamItem2"),
                "cookieParam"
            )) {
                is ParametersOptionalNotNullHttpResponse.Ok -> {
                    Assertions.assertThat(response.headerSingleValue).isEqualTo("headerParam")
                    Assertions.assertThat(response.headerCollectionValue)
                        .containsExactly("headerParamItem1", "headerParamItem2")
                    Assertions.assertThat(response.safeBody.querySingleValue).isEqualTo("queryParam")
                    Assertions.assertThat(response.safeBody.queryCollectionValue)
                        .containsExactly("queryParamItem1", "queryParamItem2")
                    Assertions.assertThat(response.safeBody.headerSingleValue).isEqualTo("headerParam")
                    Assertions.assertThat(response.safeBody.headerCollectionValue)
                        .containsExactly("headerParamItem1", "headerParamItem2")
                    Assertions.assertThat(response.safeBody.cookieSingleValue).isEqualTo("cookieParam")
                }

                is ParametersOptionalNotNullHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `optional non null parameters with null values work`() {
        runBlocking {
            when (val response = client.parametersOptionalNotNull()) {
                is ParametersOptionalNotNullHttpResponse.Ok -> {
                    Assertions.assertThat(response.headerSingleValue).isNull()
                    Assertions.assertThat(response.headerCollectionValue).isNull()
                    Assertions.assertThat(response.safeBody.querySingleValue).isNull()
                    Assertions.assertThat(response.safeBody.queryCollectionValue).isEmpty()
                    Assertions.assertThat(response.safeBody.headerSingleValue).isNull()
                    Assertions.assertThat(response.safeBody.headerCollectionValue).isEmpty()
                    Assertions.assertThat(response.safeBody.cookieSingleValue).isNull()
                }

                is ParametersOptionalNotNullHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `optional nullable parameters with real values work`() {
        runBlocking {
            when (val response = client.parametersOptionalNullable(
                "queryParam",
                listOf("queryParamItem1", "queryParamItem2"),
                "headerParam",
                listOf("headerParamItem1", "headerParamItem2"),
                "cookieParam"
            )) {
                is ParametersOptionalNullableHttpResponse.Ok -> {
                    Assertions.assertThat(response.headerSingleValue).isEqualTo("headerParam")
                    Assertions.assertThat(response.headerCollectionValue)
                        .containsExactly("headerParamItem1", "headerParamItem2")
                    Assertions.assertThat(response.safeBody.querySingleValue).isEqualTo("queryParam")
                    Assertions.assertThat(response.safeBody.queryCollectionValue)
                        .containsExactly("queryParamItem1", "queryParamItem2")
                    Assertions.assertThat(response.safeBody.headerSingleValue).isEqualTo("headerParam")
                    Assertions.assertThat(response.safeBody.headerCollectionValue)
                        .containsExactly("headerParamItem1", "headerParamItem2")
                    Assertions.assertThat(response.safeBody.cookieSingleValue).isEqualTo("cookieParam")
                }

                is ParametersOptionalNullableHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `optional nullable parameters with null values work`() {
        runBlocking {
            when (val response = client.parametersOptionalNullable()) {
                is ParametersOptionalNullableHttpResponse.Ok -> {
                    Assertions.assertThat(response.headerSingleValue).isNull()
                    Assertions.assertThat(response.headerCollectionValue).isNull()
                    Assertions.assertThat(response.safeBody.querySingleValue).isNull()
                    Assertions.assertThat(response.safeBody.queryCollectionValue).isEmpty()
                    Assertions.assertThat(response.safeBody.headerSingleValue).isNull()
                    Assertions.assertThat(response.safeBody.headerCollectionValue).isEmpty()
                    Assertions.assertThat(response.safeBody.cookieSingleValue).isNull()
                }

                is ParametersOptionalNullableHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

}