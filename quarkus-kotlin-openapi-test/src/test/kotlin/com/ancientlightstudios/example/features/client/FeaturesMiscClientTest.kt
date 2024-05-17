package com.ancientlightstudios.example.features.client

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@QuarkusTest
class FeaturesMiscClientTest {

    @Inject
    lateinit var client: FeaturesMiscClient

    @Test
    fun `Status code 422 is handled correctly by the framework`() {
        runBlocking {
            val response = client.unknownStatusCode()
            if (response !is UnknownStatusCodeHttpResponse.Status422) {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `Status code 422 is handled correctly by the framework for the second endpoint`() {
        runBlocking {
            val response = client.unknownStatusCode2()
            if (response !is UnknownStatusCode2HttpResponse.Status422) {
                fail("unexpected response")
            }
        }
    }
}