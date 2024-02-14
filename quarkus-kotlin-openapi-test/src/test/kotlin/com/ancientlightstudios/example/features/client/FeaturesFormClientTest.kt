package com.ancientlightstudios.example.features.client

import com.ancientlightstudios.example.features.client.model.SimpleEnum
import com.ancientlightstudios.example.features.client.model.SimpleForm
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@QuarkusTest
class FeaturesFormClientTest {

    @Inject
    lateinit var client: FeaturesFormClient

    @Test
    fun `sending a value is accepted`() {
        runBlocking {
            when (val response = client.formRequiredObject(
                SimpleForm("foo", SimpleEnum.Second)
            )) {
                is FormRequiredObjectHttpResponse.Ok -> {
                    assertThat(response.safeBody.name).isEqualTo("foo")
                    assertThat(response.safeBody.status).isEqualTo(SimpleEnum.Second)
                }

                is JsonRequiredObjectHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

}