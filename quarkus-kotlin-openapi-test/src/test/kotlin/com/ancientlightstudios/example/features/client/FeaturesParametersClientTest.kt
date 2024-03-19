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
    fun `sending a value is accepted`() {
        runBlocking {
            when (val response = client.parametersTest1("first", 45, 90)) {
                is ParametersTest1HttpResponse.NoContent -> {
                    Assertions.assertThat(response.xFirstHeader).isEqualTo("first")
                    Assertions.assertThat(response.xSecondHeader).isEqualTo(45)
                    Assertions.assertThat(response.xThirdHeader).isEqualTo(90)
                }

                is ParametersTest1HttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `sending a value is accepted2`() {
        runBlocking {
            when (val response = client.parametersTest2(listOf("foo", "bar"), listOf(90, 91))) {
                is ParametersTest2HttpResponse.NoContent -> {
                    Assertions.assertThat(response.xFirstHeader).containsExactly("foo", "bar")
                    Assertions.assertThat(response.xSecondHeader).containsExactly(90, 91)
                }

                is ParametersTest2HttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

}