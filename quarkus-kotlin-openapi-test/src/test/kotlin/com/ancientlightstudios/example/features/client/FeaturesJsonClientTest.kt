package com.ancientlightstudios.example.features.client

import com.ancientlightstudios.example.features.client.model.SimpleEnum
import com.ancientlightstudios.example.features.client.model.SimpleObject
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@QuarkusTest
class FeaturesJsonClientTest {

    @Inject
    lateinit var client: FeaturesJsonClient

    @Test
    fun `sending a value is accepted`() {
        runBlocking {
            when (val response = client.jsonRequiredObject(
                SimpleObject(
                    statusRequired = SimpleEnum.Second,
                    itemsRequired = listOf("one")
                )
            )) {
                is JsonRequiredObjectHttpResponse.Ok -> {
                    assertThat(response.safeBody.nameOptional).isEqualTo("i am optional")
                    assertThat(response.safeBody.nameRequired).isEqualTo("i am required")
                    assertThat(response.safeBody.statusOptional).isNull()
                    assertThat(response.safeBody.statusRequired).isEqualTo(SimpleEnum.Second)
                    assertThat(response.safeBody.itemsOptional).isNull()
                    assertThat(response.safeBody.itemsRequired).containsExactly("one")
                }

                is JsonRequiredObjectHttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

}