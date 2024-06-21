package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.FeaturesPlainClient
import com.ancientlightstudios.example.features.client.PlainEnumBodyHttpResponse
import com.ancientlightstudios.example.features.client.PlainEnumParameterHttpResponse
import com.ancientlightstudios.example.features.client.model.PlainEnum
import com.ancientlightstudios.example.features.testclient.FeaturesPlainTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import com.ancientlightstudios.example.features.testclient.model.PlainEnum as TestPlainEnum

@QuarkusTest
class FeaturesPlainTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesPlainClient

    val testClient: FeaturesPlainTestClient
        get() = FeaturesPlainTestClient(objectMapper) { prepareRequest() }

//    @Test
//    fun `empty parameter and empty response is supported (Client)`() {
//        runBlocking {
//            val response = client.plainEnumParameter()
//            if (response is PlainEnumParameterHttpResponse.Ok) {
//                assertThat(response.safeBody).isNull()
//            } else {
//                fail("unexpected response")
//            }
//        }
//    }
//
//    @Test
//    fun `empty parameter and empty response is supported (Test-Client)`() {
//        testClient.plainEnumParameterSafe(null)
//            .isOkResponse {
//                assertThat(safeBody).isNull()
//            }
//    }
//
//    @Test
//    fun `empty parameter and empty response is supported (Raw)`() {
//        prepareRequest()
//            .get("/features/plain/plainEnumParameter")
//            .execute()
//            .statusCode(200)
//            .body(equalTo(null))
//    }
//
//    @ParameterizedTest
//    @EnumSource(value = PlainEnum::class)
//    fun `enum item as parameter is supported (Client)`(value: PlainEnum) {
//        runBlocking {
//            val response = client.plainEnumParameter(value)
//            if (response is PlainEnumParameterHttpResponse.Ok) {
//                assertThat(response.safeBody).isEqualTo(value)
//            } else {
//                fail("unexpected response")
//            }
//        }
//    }
//
//    @ParameterizedTest
//    @EnumSource(value = TestPlainEnum::class)
//    fun `enum item as parameter is supported (Test-Client)`(value: TestPlainEnum) {
//        testClient.plainEnumParameterSafe(value)
//            .isOkResponse {
//                assertThat(safeBody).isEqualTo(value)
//            }
//    }
//
//    @ParameterizedTest
//    @EnumSource(value = PlainEnum::class)
//    fun `enum item as parameter is supported (Raw)`(value: PlainEnum) {
//        prepareRequest()
//            .queryParam("param", value.value)
//            .get("/features/plain/plainEnumParameter")
//            .execute()
//            .statusCode(200)
//            .body(equalTo(value.value.toString()))
//    }
//
//    @Test
//    fun `empty request is supported (Client)`() {
//        runBlocking {
//            val response = client.plainEnumBody()
//            if (response is PlainEnumBodyHttpResponse.Ok) {
//                assertThat(response.safeBody).isNull()
//            } else {
//                fail("unexpected response")
//            }
//        }
//    }
//
//    @Test
//    fun `empty request is supported (Test-Client)`() {
//        testClient.plainEnumBodySafe(null)
//            .isOkResponse {
//                assertThat(safeBody).isNull()
//            }
//    }
//
//    @Test
//    fun `empty request is supported (Raw)`() {
//        prepareRequest()
//            .get("/features/plain/plainEnumBody")
//            .execute()
//            .statusCode(200)
//            .body(equalTo(null))
//    }
//
//    @ParameterizedTest
//    @EnumSource(value = PlainEnum::class)
//    fun `enum item as request is supported (Client)`(value: PlainEnum) {
//        runBlocking {
//            val response = client.plainEnumBody(value)
//            if (response is PlainEnumBodyHttpResponse.Ok) {
//                assertThat(response.safeBody).isEqualTo(value)
//            } else {
//                fail("unexpected response")
//            }
//        }
//    }
//
//    @ParameterizedTest
//    @EnumSource(value = TestPlainEnum::class)
//    fun `enum item as request is supported (Test-Client)`(value: TestPlainEnum) {
//        testClient.plainEnumBodySafe(value)
//            .isOkResponse {
//                assertThat(safeBody).isEqualTo(value)
//            }
//    }
//
//    @ParameterizedTest
//    @EnumSource(value = PlainEnum::class)
//    fun `enum item as request is supported (Raw)`(value: PlainEnum) {
//        prepareRequest()
//            .queryParam("param", value.value)
//            .get("/features/plain/plainEnumBody")
//            .execute()
//            .statusCode(200)
//            .body(equalTo(value.value.toString()))
//    }
}