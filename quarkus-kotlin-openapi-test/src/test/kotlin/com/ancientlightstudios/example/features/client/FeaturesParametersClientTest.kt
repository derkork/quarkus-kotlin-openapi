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
            when (val response = client.parametersTest("first", 45, 90)) {
                is ParametersTestHttpResponse.NoContent -> {
                    Assertions.assertThat(response.xFirstHeader).isEqualTo("first")
                    Assertions.assertThat(response.xSecondHeader).isEqualTo(45)
                    Assertions.assertThat(response.xThirdHeader).isEqualTo(90)
                }

                is ParametersTestHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

}