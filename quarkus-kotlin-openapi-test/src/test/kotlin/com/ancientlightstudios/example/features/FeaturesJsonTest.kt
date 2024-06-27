package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.FeaturesJsonClient
import com.ancientlightstudios.example.features.client.JsonOptionalObjectHttpResponse
import com.ancientlightstudios.example.features.client.JsonRequiredObjectHttpResponse
import com.ancientlightstudios.example.features.client.model.JsonEnum
import com.ancientlightstudios.example.features.client.model.SimpleObject
import com.ancientlightstudios.example.features.testclient.FeaturesJsonTestClient
import com.ancientlightstudios.quarkus.kotlin.openapi.UnsafeJson
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import com.ancientlightstudios.example.features.testclient.model.JsonEnum as TestJsonEnum
import com.ancientlightstudios.example.features.testclient.model.SimpleObject as TestSimpleObject

@QuarkusTest
class FeaturesJsonTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesJsonClient

    val testClient: FeaturesJsonTestClient
        get() = FeaturesJsonTestClient(objectMapper) { prepareRequest() }

    @Test
    fun `sending null as an optional body works (Client)`() {
        runBlocking {
            val response = client.jsonOptionalObject(null)
            if (response is JsonOptionalObjectHttpResponse.Ok) {
                assertThat(response.safeBody).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending null as an optional body works (Test-Client)`() {
        testClient.jsonOptionalObjectSafe(null)
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending null as an optional body works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body("null")
            .post("/features/json/optional/object")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending nothing as an optional body works (Test-Client)`() {
        testClient.jsonOptionalObjectRaw {
            contentType("application/json")
        }
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending nothing as an optional body works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .post("/features/json/optional/object")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending null as a required body is rejected (Test-Client)`() {
        testClient.jsonRequiredObjectUnsafe {
            body(null as UnsafeJson<TestSimpleObject>?)
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "required"))
            }
    }

    @Test
    fun `sending null as a required body is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body("null")
            .post("/features/json/required/object")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "required"))
    }

    @Test
    fun `sending nothing as a required body is rejected (Test-Client)`() {
        testClient.jsonRequiredObjectRaw {
            contentType("application/json")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "required"))
            }
    }

    @Test
    fun `sending nothing as a required body is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .post("/features/json/required/object")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "required"))
    }

    @Test
    fun `sending a value is accepted (Client)`() {
        runBlocking {
            val response = client.jsonRequiredObject(
                SimpleObject(
                    statusRequired = JsonEnum.Second,
                    itemsRequired = listOf(10)
                )
            )

            if (response is JsonRequiredObjectHttpResponse.Ok) {
                assertThat(response.safeBody.nameOptional).isEqualTo("i am optional")
                assertThat(response.safeBody.nameRequired).isEqualTo("i am required")
                assertThat(response.safeBody.statusOptional).isNull()
                assertThat(response.safeBody.statusRequired).isEqualTo(JsonEnum.Second)
                assertThat(response.safeBody.itemsOptional).isNull()
                assertThat(response.safeBody.itemsRequired).containsExactly(10)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending a value is accepted (Test-Client)`() {
        testClient.jsonRequiredObjectSafe(
            TestSimpleObject(
                statusRequired = TestJsonEnum.Second,
                itemsRequired = listOf(10)
            )
        ).isOkResponse {
            assertThat(safeBody.nameOptional).isEqualTo("i am optional")
            assertThat(safeBody.nameRequired).isEqualTo("i am required")
            assertThat(safeBody.statusOptional).isNull()
            assertThat(safeBody.statusRequired).isEqualTo(TestJsonEnum.Second)
            assertThat(safeBody.itemsOptional).isNull()
            assertThat(safeBody.itemsRequired).containsExactly(10)
        }
    }

    @Test
    fun `sending a value is accepted (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "statusRequired": "second",
                  "itemsRequired": [10]
                }""".trimIndent()
            )
            .post("/features/json/required/object")
            .execute()
            .statusCode(200)
            .body("nameOptional", equalTo("i am optional"))
            .body("nameRequired", equalTo("i am required"))
            .body("statusOptional", equalTo(null))
            .body("statusRequired", equalTo(JsonEnum.Second.value))
            .body("itemsOptional", equalTo(null))
            .body("itemsRequired", containsInAnyOrder(10))
    }

    @Test
    fun `sending an incompatible value is rejected (Test-Client)`() {
        testClient.jsonRequiredObjectRaw {
            contentType("application/json")
                .body("true")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "is not a valid json object"))
            }
    }

    @Test
    fun `sending an incompatible value is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body("true")
            .post("/features/json/required/object")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "is not a valid json object"))
    }

    @Test
    fun `sending an invalid value is rejected (Test-Client)`() {
        testClient.jsonRequiredObjectUnsafe {
            body(TestSimpleObject.unsafeJson(nameRequired = "foo", statusRequired = TestJsonEnum.TheFirst))
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body.itemsRequired", "is required"))
            }
    }

    @Test
    fun `sending an invalid value is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "nameRequired": "foo",
                  "statusRequired": "first"
                }""".trimIndent()
            )
            .post("/features/json/required/object")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body.itemsRequired", "is required"))
    }

}