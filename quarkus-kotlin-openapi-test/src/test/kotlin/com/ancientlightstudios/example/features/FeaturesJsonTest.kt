package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.*
import com.ancientlightstudios.example.features.client.model.JsonEnum
import com.ancientlightstudios.example.features.client.model.SimpleObject
import com.ancientlightstudios.example.features.testclient.FeaturesJsonTestClient
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
    fun `sending null as an optional object body works (Client)`() {
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
    fun `sending null as an optional object body works (Test-Client)`() {
        testClient.jsonOptionalObjectSafe(null)
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending null as an optional object body works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body("null")
            .post("/features/json/optional/object")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending nothing as an optional object body works (Test-Client)`() {
        testClient.jsonOptionalObjectUnsafe {}
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending nothing as an optional object body works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .post("/features/json/optional/object")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending null as a required object body is rejected (Test-Client)`() {
        testClient.jsonRequiredObjectUnsafe {
            body(null)
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "required"))
            }
    }

    @Test
    fun `sending null as a required object body is rejected (Raw)`() {
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
    fun `sending nothing as a required object body is rejected (Test-Client)`() {
        testClient.jsonRequiredObjectUnsafe {}
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "required"))
            }
    }

    @Test
    fun `sending nothing as a required object body is rejected (Raw)`() {
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
    fun `sending an object value is accepted (Client)`() {
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
    fun `sending an object value is accepted (Test-Client)`() {
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
    fun `sending an object value is accepted (Raw)`() {
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
    fun `sending an incompatible object value is rejected (Test-Client)`() {
        testClient.jsonRequiredObjectRaw {
            contentType("application/json")
                .body("true")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "is not a valid json object"))
            }
    }

    @Test
    fun `sending an incompatible object value is rejected (Raw)`() {
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
    fun `sending an invalid object value is rejected (Test-Client)`() {
        testClient.jsonRequiredObjectUnsafe {
            body(TestSimpleObject.unsafeJson(nameRequired = "foo", statusRequired = TestJsonEnum.TheFirst))
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body.itemsRequired", "is required"))
            }
    }

    @Test
    fun `sending an invalid object value is rejected (Raw)`() {
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

    @Test
    fun `sending null as an optional array body works (Client)`() {
        runBlocking {
            val response = client.jsonOptionalArray(null)
            if (response is JsonOptionalArrayHttpResponse.Ok) {
                assertThat(response.safeBody).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending null as an optional array body works (Test-Client)`() {
        testClient.jsonOptionalArraySafe(null)
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending null as an optional array body works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body("null")
            .post("/features/json/optional/array")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending nothing as an optional array body works (Test-Client)`() {
        testClient.jsonOptionalArrayUnsafe {}
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending nothing as an optional array body works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .post("/features/json/optional/array")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending null as a required array body is rejected (Test-Client)`() {
        testClient.jsonRequiredArrayUnsafe {
            body(null)
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "required"))
            }
    }

    @Test
    fun `sending null as a required array body is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body("null")
            .post("/features/json/required/array")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "required"))
    }

    @Test
    fun `sending nothing as a required array body is rejected (Test-Client)`() {
        testClient.jsonRequiredArrayUnsafe {}
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "required"))
            }
    }

    @Test
    fun `sending nothing as a required array body is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .post("/features/json/required/array")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "required"))
    }

    @Test
    fun `sending an empty array value is accepted (Client)`() {
        runBlocking {
            val response = client.jsonRequiredArray(listOf())

            if (response is JsonRequiredArrayHttpResponse.Ok) {
                assertThat(response.safeBody.size).isEqualTo(0)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending an empty array value is accepted (Test-Client)`() {
        testClient.jsonRequiredArraySafe(listOf())
            .isOkResponse {
                assertThat(safeBody.size).isEqualTo(0)
            }
    }

    @Test
    fun `sending an empty array value is accepted (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body("[]")
            .post("/features/json/required/array")
            .execute()
            .statusCode(200)
            .body("size()", equalTo(0))
    }

    @Test
    fun `sending an array value is accepted (Client)`() {
        runBlocking {
            val response = client.jsonRequiredArray(
                listOf(
                    SimpleObject(
                        statusRequired = JsonEnum.Second,
                        itemsRequired = listOf(10)
                    )
                )
            )

            if (response is JsonRequiredArrayHttpResponse.Ok) {
                assertThat(response.safeBody.size).isEqualTo(1)
                val first = response.safeBody.first()
                assertThat(first.nameOptional).isEqualTo("i am optional")
                assertThat(first.nameRequired).isEqualTo("i am required")
                assertThat(first.statusOptional).isNull()
                assertThat(first.statusRequired).isEqualTo(JsonEnum.Second)
                assertThat(first.itemsOptional).isNull()
                assertThat(first.itemsRequired).containsExactly(10)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending an array value is accepted (Test-Client)`() {
        testClient.jsonRequiredArraySafe(
            listOf(
                TestSimpleObject(
                    statusRequired = TestJsonEnum.Second,
                    itemsRequired = listOf(10)
                )
            )
        ).isOkResponse {
            assertThat(safeBody.size).isEqualTo(1)
            val first = safeBody.first()
            assertThat(first.nameOptional).isEqualTo("i am optional")
            assertThat(first.nameRequired).isEqualTo("i am required")
            assertThat(first.statusOptional).isNull()
            assertThat(first.statusRequired).isEqualTo(TestJsonEnum.Second)
            assertThat(first.itemsOptional).isNull()
            assertThat(first.itemsRequired).containsExactly(10)
        }
    }

    @Test
    fun `sending an array value is accepted (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """[{
                  "statusRequired": "second",
                  "itemsRequired": [10]
                }]""".trimIndent()
            )
            .post("/features/json/required/array")
            .execute()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].nameOptional", equalTo("i am optional"))
            .body("[0].nameRequired", equalTo("i am required"))
            .body("[0].statusOptional", equalTo(null))
            .body("[0].statusRequired", equalTo(JsonEnum.Second.value))
            .body("[0].itemsOptional", equalTo(null))
            .body("[0].itemsRequired", containsInAnyOrder(10))
    }

    @Test
    fun `sending an incompatible array value is rejected (Test-Client)`() {
        testClient.jsonRequiredArrayRaw {
            contentType("application/json")
                .body("true")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "is not a valid json array"))
            }
    }

    @Test
    fun `sending an incompatible array value is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body("true")
            .post("/features/json/required/array")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "is not a valid json array"))
    }

    @Test
    fun `sending an invalid array value is rejected (Test-Client)`() {
        testClient.jsonRequiredArrayUnsafe {
            body(listOf(TestSimpleObject.unsafeJson(nameRequired = "foo", statusRequired = TestJsonEnum.TheFirst)))
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body[0].itemsRequired", "is required"))
            }
    }

    @Test
    fun `sending an invalid array value is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """[{
                  "nameRequired": "foo",
                  "statusRequired": "first"
                }]""".trimIndent()
            )
            .post("/features/json/required/array")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body[0].itemsRequired", "is required"))
    }

}