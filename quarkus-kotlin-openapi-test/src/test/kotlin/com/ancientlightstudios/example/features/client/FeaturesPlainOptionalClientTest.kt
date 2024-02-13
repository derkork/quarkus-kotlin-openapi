package com.ancientlightstudios.example.features.client

import com.ancientlightstudios.example.features.client.model.SimpleEnum
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@QuarkusTest
class FeaturesPlainOptionalClientTest {

    @Inject
    lateinit var client: FeaturesPlainOptionalClient

    @Test
    fun `sending a value is accepted`() {
        runBlocking {
            when (val response = client.plainOptionalEnum(SimpleEnum.Second)) {
                is PlainOptionalEnumHttpResponse.Ok -> assertThat(response.safeBody).isEqualTo(SimpleEnum.Second)
                is PlainOptionalEnumHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

}