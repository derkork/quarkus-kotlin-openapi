package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.*
import com.ancientlightstudios.example.features.client.model.*
import com.ancientlightstudios.example.features.testclient.FeaturesJsonTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import com.ancientlightstudios.example.features.testclient.model.Container as TestContainer
import com.ancientlightstudios.example.features.testclient.model.JsonEnum as TestJsonEnum
import com.ancientlightstudios.example.features.testclient.model.NonNullContainerPart as TestNonNullContainerPart
import com.ancientlightstudios.example.features.testclient.model.NullableContainerPart as TestNullableContainerPart
import com.ancientlightstudios.example.features.testclient.model.SimpleObject as TestSimpleObject

@QuarkusTest
class FeaturesJsonTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesJsonClient

    val testClient: FeaturesJsonTestClient
        get() = FeaturesJsonTestClient(dependencyContainer) { prepareRequest() }

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
            body("true")
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
            body("true")
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

    @Test
    fun `sending null as an optional simple map body works (Client)`() {
        runBlocking {
            val response = client.jsonOptionalMap(null)
            if (response is JsonOptionalMapHttpResponse.Ok) {
                assertThat(response.safeBody).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending null as an optional simple map body works (Test-Client)`() {
        testClient.jsonOptionalMapSafe(null)
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending null as an optional simple map body works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body("null")
            .post("/features/json/optional/map")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending nothing as an optional simple map body works (Test-Client)`() {
        testClient.jsonOptionalMapUnsafe {}
            .isOkResponse {
                assertThat(safeBody).isNull()
            }
    }

    @Test
    fun `sending nothing as an optional simple map body works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .post("/features/json/optional/map")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending null as a required simple map body is rejected (Test-Client)`() {
        testClient.jsonRequiredMapUnsafe {
            body(null)
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "required"))
            }
    }

    @Test
    fun `sending null as a required simple map body is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body("null")
            .post("/features/json/required/map")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "required"))
    }

    @Test
    fun `sending nothing as a required simple map body is rejected (Test-Client)`() {
        testClient.jsonRequiredMapUnsafe {}
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "required"))
            }
    }

    @Test
    fun `sending nothing as a required simple map body is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .post("/features/json/required/map")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "required"))
    }

    @Test
    fun `sending an empty simple map value is accepted (Client)`() {
        runBlocking {
            val response = client.jsonRequiredMap(mapOf())

            if (response is JsonRequiredMapHttpResponse.Ok) {
                assertThat(response.safeBody.size).isEqualTo(0)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending an empty simple map value is accepted (Test-Client)`() {
        testClient.jsonRequiredMapSafe(mapOf())
            .isOkResponse {
                assertThat(safeBody.size).isEqualTo(0)
            }
    }

    @Test
    fun `sending an empty simple map value is accepted (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body("{}")
            .post("/features/json/required/map")
            .execute()
            .statusCode(200)
            .body("size()", equalTo(0))
    }

    @Test
    fun `sending an simple map value is accepted (Client)`() {
        runBlocking {
            val response = client.jsonRequiredMap(
                mapOf(
                    "first" to SimpleObject(
                        statusRequired = JsonEnum.Second,
                        itemsRequired = listOf(10)
                    )
                )
            )

            if (response is JsonRequiredMapHttpResponse.Ok) {
                assertThat(response.safeBody).hasSize(1)
                val first = response.safeBody["first"] ?: fail("map entry not found")
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
    fun `sending an simple map value is accepted (Test-Client)`() {
        testClient.jsonRequiredMapSafe(
            mapOf(
                "first" to TestSimpleObject(
                    statusRequired = TestJsonEnum.Second,
                    itemsRequired = listOf(10)
                )
            )
        ).isOkResponse {
            assertThat(safeBody).hasSize(1)
            val first = safeBody["first"] ?: fail("map entry not found")
            assertThat(first.nameOptional).isEqualTo("i am optional")
            assertThat(first.nameRequired).isEqualTo("i am required")
            assertThat(first.statusOptional).isNull()
            assertThat(first.statusRequired).isEqualTo(TestJsonEnum.Second)
            assertThat(first.itemsOptional).isNull()
            assertThat(first.itemsRequired).containsExactly(10)
        }
    }

    @Test
    fun `sending an simple map value is accepted (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                     "first": {
                       "statusRequired": "second",
                       "itemsRequired": [10]
                    }
                }""".trimIndent()
            )
            .post("/features/json/required/map")
            .execute()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("first.nameOptional", equalTo("i am optional"))
            .body("first.nameRequired", equalTo("i am required"))
            .body("first.statusOptional", equalTo(null))
            .body("first.statusRequired", equalTo(JsonEnum.Second.value))
            .body("first.itemsOptional", equalTo(null))
            .body("first.itemsRequired", containsInAnyOrder(10))
    }

    @Test
    fun `sending an incompatible simple map value is rejected (Test-Client)`() {
        testClient.jsonRequiredMapRaw {
            body("true")
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "is not a valid json object"))
            }
    }

    @Test
    fun `sending an incompatible simple map value is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body("true")
            .post("/features/json/required/map")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "is not a valid json object"))
    }

    @Test
    fun `sending an invalid simple map value is rejected (Test-Client)`() {
        testClient.jsonRequiredMapUnsafe {
            body(
                mapOf(
                    "first" to TestSimpleObject.unsafeJson(
                        nameRequired = "foo",
                        statusRequired = TestJsonEnum.TheFirst
                    )
                )
            )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body.first.itemsRequired", "is required"))
            }
    }

    @Test
    fun `sending an invalid simple map value is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "first": {
                      "nameRequired": "foo",
                      "statusRequired": "first"
                    }
                }""".trimIndent()
            )
            .post("/features/json/required/map")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body.first.itemsRequired", "is required"))
    }

    @Test
    fun `sending no additional properties works (Client)`() {
        runBlocking {
            val response = client.jsonNestedMap(
                Container(
                    NonNullContainerPart("foo", 10L),
                    NullableContainerPart("foo2", 12L)
                )
            )

            if (response is JsonNestedMapHttpResponse.Ok) {
                assertThat(response.safeBody.withNonNullValues.foo).isEqualTo("foo")
                assertThat(response.safeBody.withNonNullValues.bar).isEqualTo(10L)
                assertThat(response.safeBody.withNonNullValues.additionalProperties).isEmpty()
                assertThat(response.safeBody.withNullableValues.foo).isEqualTo("foo2")
                assertThat(response.safeBody.withNullableValues.bar).isEqualTo(12L)
                assertThat(response.safeBody.withNullableValues.additionalProperties).isEmpty()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending no additional properties works (Test-Client)`() {
        testClient.jsonNestedMapSafe(
            TestContainer(
                TestNonNullContainerPart("foo", 10L),
                TestNullableContainerPart("foo2", 12L)
            )
        )
            .isOkResponse {
                assertThat(safeBody.withNonNullValues.foo).isEqualTo("foo")
                assertThat(safeBody.withNonNullValues.bar).isEqualTo(10L)
                assertThat(safeBody.withNonNullValues.additionalProperties).isEmpty()
                assertThat(safeBody.withNullableValues.foo).isEqualTo("foo2")
                assertThat(safeBody.withNullableValues.bar).isEqualTo(12L)
                assertThat(safeBody.withNullableValues.additionalProperties).isEmpty()
            }
    }

    @Test
    fun `sending no additional properties works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "withNonNullValues": {
                    "foo": "foo",
                    "bar": 10
                  },
                  "withNullableValues": {
                    "foo": "foo2",
                    "bar": 12
                  }
                }""".trimIndent()
            )
            .post("/features/json/nestedMap")
            .execute()
            .statusCode(200)
            .body("withNonNullValues.foo", equalTo("foo"))
            .body("withNonNullValues.bar", equalTo(10))
            .body("withNonNullValues.size()", equalTo(2))
            .body("withNullableValues.foo", equalTo("foo2"))
            .body("withNullableValues.bar", equalTo(12))
            .body("withNullableValues.size()", equalTo(2))
    }

    @Test
    fun `sending additional properties works (Client)`() {
        runBlocking {
            val response = client.jsonNestedMap(
                Container(
                    NonNullContainerPart("foo", 10L, mapOf("first" to 20, "second" to 21)),
                    NullableContainerPart("foo2", 12L, mapOf("1st" to 30, "2nd" to 31))
                )
            )

            if (response is JsonNestedMapHttpResponse.Ok) {
                assertThat(response.safeBody.withNonNullValues.foo).isEqualTo("foo")
                assertThat(response.safeBody.withNonNullValues.bar).isEqualTo(10L)
                assertThat(response.safeBody.withNonNullValues.additionalProperties).hasSize(2)
                assertThat(response.safeBody.withNonNullValues.additionalProperties["first"]).isEqualTo(20)
                assertThat(response.safeBody.withNonNullValues.additionalProperties["second"]).isEqualTo(21)
                assertThat(response.safeBody.withNullableValues.foo).isEqualTo("foo2")
                assertThat(response.safeBody.withNullableValues.bar).isEqualTo(12L)
                assertThat(response.safeBody.withNullableValues.additionalProperties).hasSize(2)
                assertThat(response.safeBody.withNullableValues.additionalProperties["1st"]).isEqualTo(30)
                assertThat(response.safeBody.withNullableValues.additionalProperties["2nd"]).isEqualTo(31)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending additional properties works (Test-Client)`() {
        testClient.jsonNestedMapSafe(
            TestContainer(
                TestNonNullContainerPart("foo", 10L, mapOf("first" to 20, "second" to 21)),
                TestNullableContainerPart("foo2", 12L, mapOf("1st" to 30, "2nd" to 31))
            )
        )
            .isOkResponse {
                assertThat(safeBody.withNonNullValues.foo).isEqualTo("foo")
                assertThat(safeBody.withNonNullValues.bar).isEqualTo(10L)
                assertThat(safeBody.withNonNullValues.additionalProperties).hasSize(2)
                assertThat(safeBody.withNonNullValues.additionalProperties["first"]).isEqualTo(20)
                assertThat(safeBody.withNonNullValues.additionalProperties["second"]).isEqualTo(21)
                assertThat(safeBody.withNullableValues.foo).isEqualTo("foo2")
                assertThat(safeBody.withNullableValues.bar).isEqualTo(12L)
                assertThat(safeBody.withNullableValues.additionalProperties).hasSize(2)
                assertThat(safeBody.withNullableValues.additionalProperties["1st"]).isEqualTo(30)
                assertThat(safeBody.withNullableValues.additionalProperties["2nd"]).isEqualTo(31)
            }
    }

    @Test
    fun `sending additional properties works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "withNonNullValues": {
                    "foo": "foo",
                    "bar": 10,
                    "first": 20,
                    "second": 21
                  },
                  "withNullableValues": {
                    "foo": "foo2",
                    "bar": 12,
                    "1st": 30,
                    "2nd": 31
                  }
                }""".trimIndent()
            )
            .post("/features/json/nestedMap")
            .execute()
            .statusCode(200)
            .body("withNonNullValues.foo", equalTo("foo"))
            .body("withNonNullValues.bar", equalTo(10))
            .body("withNonNullValues.first", equalTo(20))
            .body("withNonNullValues.second", equalTo(21))
            .body("withNonNullValues.size()", equalTo(4))
            .body("withNullableValues.foo", equalTo("foo2"))
            .body("withNullableValues.bar", equalTo(12))
            .body("withNullableValues.1st", equalTo(30))
            .body("withNullableValues.2nd", equalTo(31))
            .body("withNullableValues.size()", equalTo(4))
    }

    @Test
    fun `overwriting properties with additional properties is not possible (Client)`() {
        runBlocking {
            val response = client.jsonNestedMap(
                Container(
                    NonNullContainerPart("foo", 10L, mapOf("foo" to 20, "bar" to 21, "first" to 22)),
                    NullableContainerPart("foo2", 12L, mapOf("foo" to 30, "bar" to 31, "1st" to 32))
                )
            )

            if (response is JsonNestedMapHttpResponse.Ok) {
                assertThat(response.safeBody.withNonNullValues.foo).isEqualTo("foo")
                assertThat(response.safeBody.withNonNullValues.bar).isEqualTo(10L)
                assertThat(response.safeBody.withNonNullValues.additionalProperties).hasSize(1)
                assertThat(response.safeBody.withNonNullValues.additionalProperties["first"]).isEqualTo(22)
                assertThat(response.safeBody.withNullableValues.foo).isEqualTo("foo2")
                assertThat(response.safeBody.withNullableValues.bar).isEqualTo(12L)
                assertThat(response.safeBody.withNullableValues.additionalProperties).hasSize(1)
                assertThat(response.safeBody.withNullableValues.additionalProperties["1st"]).isEqualTo(32)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `overwriting properties with additional properties is not possible (Test-Client)`() {
        testClient.jsonNestedMapSafe(
            TestContainer(
                TestNonNullContainerPart("foo", 10L, mapOf("foo" to 20, "bar" to 21, "first" to 22)),
                TestNullableContainerPart("foo2", 12L, mapOf("foo" to 30, "bar" to 31, "1st" to 32))
            )
        )
            .isOkResponse {
                assertThat(safeBody.withNonNullValues.foo).isEqualTo("foo")
                assertThat(safeBody.withNonNullValues.bar).isEqualTo(10L)
                assertThat(safeBody.withNonNullValues.additionalProperties).hasSize(1)
                assertThat(safeBody.withNonNullValues.additionalProperties["first"]).isEqualTo(22)
                assertThat(safeBody.withNullableValues.foo).isEqualTo("foo2")
                assertThat(safeBody.withNullableValues.bar).isEqualTo(12L)
                assertThat(safeBody.withNullableValues.additionalProperties).hasSize(1)
                assertThat(safeBody.withNullableValues.additionalProperties["1st"]).isEqualTo(32)
            }
    }

    @Test
    fun `sending null as an additional property with null support works (Client)`() {
        runBlocking {
            val response = client.jsonNestedMap(
                Container(
                    NonNullContainerPart("foo", 10L),
                    NullableContainerPart("foo2", 12L, mapOf("1st" to null))
                )
            )

            if (response is JsonNestedMapHttpResponse.Ok) {
                assertThat(response.safeBody.withNonNullValues.foo).isEqualTo("foo")
                assertThat(response.safeBody.withNonNullValues.bar).isEqualTo(10L)
                assertThat(response.safeBody.withNonNullValues.additionalProperties).isEmpty()
                assertThat(response.safeBody.withNullableValues.foo).isEqualTo("foo2")
                assertThat(response.safeBody.withNullableValues.bar).isEqualTo(12L)
                assertThat(response.safeBody.withNullableValues.additionalProperties).hasSize(1)
                assertThat(response.safeBody.withNullableValues.additionalProperties["1st"]).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending null as an additional property with null support works (Test-Client)`() {
        testClient.jsonNestedMapSafe(
            TestContainer(
                TestNonNullContainerPart("foo", 10L),
                TestNullableContainerPart("foo2", 12L, mapOf("1st" to null))
            )
        )
            .isOkResponse {
                assertThat(safeBody.withNonNullValues.foo).isEqualTo("foo")
                assertThat(safeBody.withNonNullValues.bar).isEqualTo(10L)
                assertThat(safeBody.withNonNullValues.additionalProperties).isEmpty()
                assertThat(safeBody.withNullableValues.foo).isEqualTo("foo2")
                assertThat(safeBody.withNullableValues.bar).isEqualTo(12L)
                assertThat(safeBody.withNullableValues.additionalProperties).hasSize(1)
                assertThat(safeBody.withNullableValues.additionalProperties["1st"]).isNull()
            }
    }

    @Test
    fun `sending null as an additional property with null support works (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "withNonNullValues": {
                    "foo": "foo",
                    "bar": 10
                  },
                  "withNullableValues": {
                    "foo": "foo2",
                    "bar": 12,
                    "1st": null
                  }
                }""".trimIndent()
            )
            .post("/features/json/nestedMap")
            .execute()
            .statusCode(200)
            .body("withNonNullValues.foo", equalTo("foo"))
            .body("withNonNullValues.bar", equalTo(10))
            .body("withNonNullValues.size()", equalTo(2))
            .body("withNullableValues.foo", equalTo("foo2"))
            .body("withNullableValues.bar", equalTo(12))
            .body("withNullableValues.1st", equalTo(null))
            .body("withNullableValues.size()", equalTo(3))
    }

    @Test
    fun `sending null as an additional property without null support is rejected (Test-Client)`() {
        testClient.jsonNestedMapUnsafe {
            body(
                TestContainer.unsafeJson(
                    TestNonNullContainerPart.unsafeJson("foo", 10L, mapOf("first" to null)),
                    TestNullableContainerPart.unsafeJson("foo2", 12L),
                )
            )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(
                    listOf(
                        "request.body.withNonNullValues.first",
                        "required"
                    )
                )
            }
    }

    @Test
    fun `sending null as an additional property without null support is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "withNonNullValues": {
                    "foo": "foo",
                    "bar": 10,
                    "first": null
                  },
                  "withNullableValues": {
                    "foo": "foo2",
                    "bar": 12
                  }
                }""".trimIndent()
            )
            .post("/features/json/nestedMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body.withNonNullValues.first", "required"))
    }

    @Test
    fun `sending an incompatible value for an additional property is rejected (Test-Client)`() {
        testClient.jsonNestedMapRaw {
            body(
                """{
                  "withNonNullValues": {
                    "foo": "foo",
                    "bar": 10,
                    "first": true
                  },
                  "withNullableValues": {
                    "foo": "foo2",
                    "bar": 12,
                    "1st": true
                  }
                }""".trimIndent()
            )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(
                    listOf("request.body.withNonNullValues.first", "not an int"),
                    listOf("request.body.withNullableValues.1st", "not an int"),
                )
            }
    }

    @Test
    fun `sending an incompatible value for an additional property is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "withNonNullValues": {
                    "foo": "foo",
                    "bar": 10,
                    "first": true
                  },
                  "withNullableValues": {
                    "foo": "foo2",
                    "bar": 12,
                    "1st": true
                  }
                }""".trimIndent()
            )
            .post("/features/json/nestedMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.body.withNonNullValues.first", "not an int"),
            listOf("request.body.withNullableValues.1st", "not an int"),
        )
    }

    @Test
    fun `sending an invalid value for an additional property is rejected (Client)`() {
        runBlocking {
            val response = client.jsonNestedMap(
                Container(
                    NonNullContainerPart("foo", 10L, mapOf("first" to 999999)),
                    NullableContainerPart("foo2", 12L, mapOf("1st" to 999999))
                )
            )

            if (response is JsonNestedMapHttpResponse.BadRequest) {
                assertThat(response.safeBody.messages).containsExactly(
                    listOf("request.body.withNonNullValues.first", "maximum"),
                    listOf("request.body.withNullableValues.1st", "maximum")
                )
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending an invalid value for an additional property is rejected (Test-Client)`() {
        testClient.jsonNestedMapSafe(
            TestContainer(
                TestNonNullContainerPart("foo", 10L, mapOf("first" to 999999)),
                TestNullableContainerPart("foo2", 12L, mapOf("1st" to 999999))
            )
        )
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(
                    listOf("request.body.withNonNullValues.first", "maximum"),
                    listOf("request.body.withNullableValues.1st", "maximum")
                )
            }
    }

    @Test
    fun `sending an invalid value for an additional property is rejected (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "withNonNullValues": {
                    "foo": "foo",
                    "bar": 10,
                    "first": 999999
                  },
                  "withNullableValues": {
                    "foo": "foo2",
                    "bar": 12,
                    "1st": 999999
                  }
                }""".trimIndent()
            )
            .post("/features/json/nestedMap")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(
            listOf("request.body.withNonNullValues.first", "maximum"),
            listOf("request.body.withNullableValues.1st", "maximum")
        )
    }

}