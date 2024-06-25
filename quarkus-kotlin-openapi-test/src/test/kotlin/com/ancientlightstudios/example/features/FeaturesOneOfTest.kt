package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.FeaturesOneOfClient
import com.ancientlightstudios.example.features.client.OneOfWithDiscriminatorAndMappingHttpResponse
import com.ancientlightstudios.example.features.client.OneOfWithDiscriminatorHttpResponse
import com.ancientlightstudios.example.features.client.OneOfWithoutDiscriminatorHttpResponse
import com.ancientlightstudios.example.features.client.model.*
import com.ancientlightstudios.example.features.testclient.FeaturesOneOfTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import com.ancientlightstudios.example.features.testclient.model.Book as TestBook
import com.ancientlightstudios.example.features.testclient.model.OneOfWithDiscriminatorAndMappingBook as TestOneOfWithDiscriminatorAndMappingBook
import com.ancientlightstudios.example.features.testclient.model.OneOfWithDiscriminatorAndMappingSong as TestOneOfWithDiscriminatorAndMappingSong
import com.ancientlightstudios.example.features.testclient.model.OneOfWithDiscriminatorBook as TestOneOfWithDiscriminatorBook
import com.ancientlightstudios.example.features.testclient.model.OneOfWithDiscriminatorSong as TestOneOfWithDiscriminatorSong
import com.ancientlightstudios.example.features.testclient.model.OneOfWithoutDiscriminatorBook as TestOneOfWithoutDiscriminatorBook
import com.ancientlightstudios.example.features.testclient.model.OneOfWithoutDiscriminatorSong as TestOneOfWithoutDiscriminatorSong
import com.ancientlightstudios.example.features.testclient.model.Song as TestSong

@QuarkusTest
class FeaturesOneOfTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesOneOfClient

    val testClient: FeaturesOneOfTestClient
        get() = FeaturesOneOfTestClient(objectMapper) { prepareRequest() }

    @Test
    fun `sending the wrong value is rejected by endpoint without discriminator (Test-Client)`() {
        testClient.oneOfWithoutDiscriminatorRaw {
            contentType("application/json")
                .body(
                    """{
                      "foo": "bar"
                    }""".trimIndent()
                )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages.size).isEqualTo(6)
            }
    }

    @Test
    fun `sending the wrong value is rejected by endpoint without discriminator (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "foo": "bar"
                }""".trimIndent()
            )
            .post("/features/oneOf/withoutDiscriminator")
            .execute()
            .statusCode(400)
            .body("messages.size()", equalTo(6))
    }

    @Test
    fun `sending an object matching all options is rejected by endpoint without discriminator (Test-Client)`() {
        testClient.oneOfWithoutDiscriminatorRaw {
            contentType("application/json")
                .body(
                    """{
                        "title": "foo",
                        "pages": 10,
                        "duration": 200,
                        "kind": "all" 
                    }""".trimMargin()
                )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("request.body", "ambiguous"))
            }
    }

    @Test
    fun `sending an object matching all options is rejected by endpoint without discriminator (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "foo",
                    "pages": 10,
                    "duration": 200,
                    "kind": "all" 
                }""".trimIndent()
            )
            .post("/features/oneOf/withoutDiscriminator")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("request.body", "ambiguous"))
    }

    @Test
    fun `sending option1 is accepted by endpoint without discriminator (Client)`() {
        runBlocking {
            val response = client.oneOfWithoutDiscriminator(
                OneOfWithoutDiscriminatorBook(Book("foo", 10, "book"))
            )

            if (response is OneOfWithoutDiscriminatorHttpResponse.Ok) {
                val safeBody = response.safeBody as? OneOfWithoutDiscriminatorBook
                    ?: fail("wrong response body")
                assertThat(safeBody.value.title).isEqualTo("foo")
                assertThat(safeBody.value.pages).isEqualTo(10)
                assertThat(safeBody.value.kind).isEqualTo("book")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending option1 is accepted by endpoint without discriminator (Test-Client)`() {
        testClient.oneOfWithoutDiscriminatorSafe(
            TestOneOfWithoutDiscriminatorBook(TestBook("foo", 10, "book"))
        )
            .isOkResponse {
                val safeBody = safeBody as? TestOneOfWithoutDiscriminatorBook
                    ?: fail("wrong response body")
                assertThat(safeBody.value.title).isEqualTo("foo")
                assertThat(safeBody.value.pages).isEqualTo(10)
                assertThat(safeBody.value.kind).isEqualTo("book")
            }
    }

    @Test
    fun `sending option1 is accepted by endpoint without discriminator (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "foo",
                    "pages": 10,
                    "kind": "book" 
                }""".trimIndent()
            )
            .post("/features/oneOf/withoutDiscriminator")
            .execute()
            .statusCode(200)
            .body("title", equalTo("foo"))
            .body("pages", equalTo(10))
            .body("kind", equalTo("book"))
    }

    @Test
    fun `sending option2 is accepted by endpoint without discriminator (Client)`() {
        runBlocking {
            val response = client.oneOfWithoutDiscriminator(
                OneOfWithoutDiscriminatorSong(Song("puit", 200, "song"))
            )

            if (response is OneOfWithoutDiscriminatorHttpResponse.Ok) {
                val safeBody = response.safeBody as? OneOfWithoutDiscriminatorSong
                    ?: fail("wrong response body")
                assertThat(safeBody.value).isNotNull
                assertThat(safeBody.value!!.title).isEqualTo("puit")
                assertThat(safeBody.value!!.duration).isEqualTo(200)
                assertThat(safeBody.value!!.kind).isEqualTo("song")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending option2 is accepted by endpoint without discriminator (Test-Client)`() {
        testClient.oneOfWithoutDiscriminatorSafe(
            TestOneOfWithoutDiscriminatorSong(TestSong("puit", 200, "song"))
        )
            .isOkResponse {
                val safeBody = safeBody as? TestOneOfWithoutDiscriminatorSong
                    ?: fail("wrong response body")
                assertThat(safeBody.value).isNotNull
                assertThat(safeBody.value!!.title).isEqualTo("puit")
                assertThat(safeBody.value!!.duration).isEqualTo(200)
                assertThat(safeBody.value!!.kind).isEqualTo("song")
            }
    }

    @Test
    fun `sending option2 is accepted by endpoint without discriminator (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "puit",
                    "duration": 200,
                    "kind": "song" 
                }""".trimIndent()
            )
            .post("/features/oneOf/withoutDiscriminator")
            .execute()
            .statusCode(200)
            .body("title", equalTo("puit"))
            .body("duration", equalTo(200))
            .body("kind", equalTo("song"))
    }

    @Test
    fun `sending null as option2 is accepted by endpoint without discriminator (Client)`() {
        runBlocking {
            val response = client.oneOfWithoutDiscriminator(
                OneOfWithoutDiscriminatorSong(null)
            )

            if (response is OneOfWithoutDiscriminatorHttpResponse.Ok) {
                val safeBody = response.safeBody as? OneOfWithoutDiscriminatorSong
                    ?: fail("wrong response body")
                assertThat(safeBody.value).isNull()
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending null as option2 is accepted by endpoint without discriminator (Test-Client)`() {
        testClient.oneOfWithoutDiscriminatorSafe(
            TestOneOfWithoutDiscriminatorSong(null)
        )
            .isOkResponse {
                val safeBody = safeBody as? TestOneOfWithoutDiscriminatorSong
                    ?: fail("wrong response body")
                assertThat(safeBody.value).isNull()
            }
    }

    // @Test
    fun `sending null as option2 is accepted by endpoint without discriminator (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .post("/features/oneOf/withoutDiscriminator")
            .execute()
            .statusCode(200)
            .body(equalTo(""))
    }

    @Test
    fun `sending the wrong discriminator is rejected by endpoint with discriminator (Test-Client)`() {
        testClient.oneOfWithDiscriminatorRaw {
            contentType("application/json")
                .body(
                    """{
                      "kind": "all"
                    }""".trimIndent()
                )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("discriminator field", "has invalid value"))
            }
    }

    @Test
    fun `sending the wrong discriminator is rejected by endpoint with discriminator (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "kind": "all"
                }""".trimIndent()
            )
            .post("/features/oneOf/withDiscriminator")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("discriminator field", "has invalid value"))
    }

    @Test
    fun `sending no discriminator is rejected by endpoint with discriminator (Test-Client)`() {
        testClient.oneOfWithDiscriminatorRaw {
            contentType("application/json")
                .body(
                    """{
                      "foo": "bar"
                    }""".trimIndent()
                )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("discriminator field", "is missing"))
            }
    }

    @Test
    fun `sending no discriminator is rejected by endpoint with discriminator (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "foo": "bar"
                }""".trimIndent()
            )
            .post("/features/oneOf/withDiscriminator")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("discriminator field", "is missing"))
    }

    @Test
    fun `sending option1 is accepted by endpoint with discriminator (Client)`() {
        runBlocking {
            val response = client.oneOfWithDiscriminator(
                OneOfWithDiscriminatorBook(Book("foo", 10, "some strange kind"))
            )

            if (response is OneOfWithDiscriminatorHttpResponse.Ok) {
                val safeBody = response.safeBody as? OneOfWithDiscriminatorBook ?: fail("wrong response body")
                assertThat(safeBody.value.title).isEqualTo("foo")
                assertThat(safeBody.value.pages).isEqualTo(10)
                assertThat(safeBody.value.kind).isEqualTo("Book")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending option1 is accepted by endpoint with discriminator (Test-Client)`() {
        testClient.oneOfWithDiscriminatorSafe(
            TestOneOfWithDiscriminatorBook(TestBook("foo", 10, "Book"))
        )
            .isOkResponse {
                val safeBody = safeBody as? TestOneOfWithDiscriminatorBook
                    ?: fail("wrong response body")
                assertThat(safeBody.value.title).isEqualTo("foo")
                assertThat(safeBody.value.pages).isEqualTo(10)
                assertThat(safeBody.value.kind).isEqualTo("Book")
            }
    }

    @Test
    fun `sending option1 is accepted by endpoint with discriminator (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "foo",
                    "pages": 10,
                    "kind": "Book" 
                }""".trimIndent()
            )
            .post("/features/oneOf/withDiscriminator")
            .execute()
            .statusCode(200)
            .body("title", equalTo("foo"))
            .body("pages", equalTo(10))
            .body("kind", equalTo("Book"))
    }

    @Test
    fun `sending option2 is accepted by endpoint with discriminator (Client)`() {
        runBlocking {
            val response = client.oneOfWithDiscriminator(
                OneOfWithDiscriminatorSong(Song("puit", 200, "some strange kind"))
            )

            if (response is OneOfWithDiscriminatorHttpResponse.Ok) {
                val safeBody = response.safeBody as? OneOfWithDiscriminatorSong
                    ?: fail("wrong response body")
                assertThat(safeBody.value).isNotNull
                assertThat(safeBody.value.title).isEqualTo("puit")
                assertThat(safeBody.value.duration).isEqualTo(200)
                assertThat(safeBody.value.kind).isEqualTo("Song")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending option2 is accepted by endpoint with discriminator (Test-Client)`() {
        testClient.oneOfWithDiscriminatorSafe(
            TestOneOfWithDiscriminatorSong(TestSong("puit", 200, "Song"))
        )
            .isOkResponse {
                val safeBody = safeBody as? TestOneOfWithDiscriminatorSong
                    ?: fail("wrong response body")
                assertThat(safeBody.value.title).isEqualTo("puit")
                assertThat(safeBody.value.duration).isEqualTo(200)
                assertThat(safeBody.value.kind).isEqualTo("Song")
            }
    }

    @Test
    fun `sending option2 is accepted by endpoint with discriminator (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "puit",
                    "duration": 200,
                    "kind": "Song" 
                }""".trimIndent()
            )
            .post("/features/oneOf/withDiscriminator")
            .execute()
            .statusCode(200)
            .body("title", equalTo("puit"))
            .body("duration", equalTo(200))
            .body("kind", equalTo("Song"))
    }

    @Test
    fun `sending the wrong discriminator is rejected by endpoint with discriminator and mapping (Test-Client)`() {
        testClient.oneOfWithDiscriminatorAndMappingRaw {
            contentType("application/json")
                .body(
                    """{
                      "kind": "all"
                    }""".trimIndent()
                )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("discriminator field", "has invalid value"))
            }
    }

    @Test
    fun `sending the wrong discriminator is rejected by endpoint with discriminator and mapping (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "kind": "all"
                }""".trimIndent()
            )
            .post("/features/oneOf/withDiscriminatorAndMapping")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("discriminator field", "has invalid value"))
    }

    @Test
    fun `sending no discriminator is rejected by endpoint with discriminator and mapping (Test-Client)`() {
        testClient.oneOfWithDiscriminatorAndMappingRaw {
            contentType("application/json")
                .body(
                    """{
                      "foo": "bar"
                    }""".trimIndent()
                )
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).containsExactly(listOf("discriminator field", "is missing"))
            }
    }

    @Test
    fun `sending no discriminator is rejected by endpoint with discriminator and mapping (Raw)`() {
        val messages = prepareRequest()
            .contentType("application/json")
            .body(
                """{
                  "foo": "bar"
                }""".trimIndent()
            )
            .post("/features/oneOf/withDiscriminatorAndMapping")
            .execute()
            .statusCode(400)
            .extract()
            .jsonPath()
            .getList<String>("messages")

        assertThat(messages).containsExactly(listOf("discriminator field", "is missing"))
    }

    @Test
    fun `sending option1 is accepted by endpoint with discriminator and mapping (Client)`() {
        runBlocking {
            val response = client.oneOfWithDiscriminatorAndMapping(
                OneOfWithDiscriminatorAndMappingBook(Book("foo", 10, "some strange kind"))
            )

            if (response is OneOfWithDiscriminatorAndMappingHttpResponse.Ok) {
                val safeBody = response.safeBody as? OneOfWithDiscriminatorAndMappingBook
                    ?: fail("wrong response body")
                assertThat(safeBody.value.title).isEqualTo("foo")
                assertThat(safeBody.value.pages).isEqualTo(10)
                assertThat(safeBody.value.kind).isEqualTo("Book")
            } else {
                fail("unexpected response")
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["Book", "booooook"])
    fun `sending option1 is accepted by endpoint with discriminator and mapping (Test-Client)`(mapping: String) {
        testClient.oneOfWithDiscriminatorAndMappingRaw {
            contentType("application/json")
                .body(
                    """{
                    "title": "foo",
                    "pages": 10,
                    "kind": "$mapping" 
                }""".trimIndent()
                )
        }
            .isOkResponse {
                val safeBody = safeBody as? TestOneOfWithDiscriminatorAndMappingBook
                    ?: fail("wrong response body")
                assertThat(safeBody.value.title).isEqualTo("foo")
                assertThat(safeBody.value.pages).isEqualTo(10)
                assertThat(safeBody.value.kind).isEqualTo("Book")
            }
    }

    @ParameterizedTest
    @ValueSource(strings = ["Book", "booooook"])
    fun `sending option1 is accepted by endpoint with discriminator and mapping (Raw)`(mapping: String) {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "foo",
                    "pages": 10,
                    "kind": "$mapping" 
                }""".trimIndent()
            )
            .post("/features/oneOf/withDiscriminatorAndMapping")
            .execute()
            .statusCode(200)
            .body("title", equalTo("foo"))
            .body("pages", equalTo(10))
            .body("kind", equalTo("Book"))
    }

    @Test
    fun `sending option2 is accepted by endpoint with discriminator and mapping (Client)`() {
        runBlocking {
            val response = client.oneOfWithDiscriminatorAndMapping(
                OneOfWithDiscriminatorAndMappingSong(Song("puit", 200, "some strange kind"))
            )

            if (response is OneOfWithDiscriminatorAndMappingHttpResponse.Ok) {
                val safeBody = response.safeBody as? OneOfWithDiscriminatorAndMappingSong
                    ?: fail("wrong response body")
                assertThat(safeBody.value).isNotNull
                assertThat(safeBody.value.title).isEqualTo("puit")
                assertThat(safeBody.value.duration).isEqualTo(200)
                assertThat(safeBody.value.kind).isEqualTo("Song")
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending option2 is accepted by endpoint with discriminator and mapping (Test-Client)`() {
        testClient.oneOfWithDiscriminatorAndMappingSafe(
            TestOneOfWithDiscriminatorAndMappingSong(TestSong("puit", 200, "Song"))
        )
            .isOkResponse {
                val safeBody = safeBody as? TestOneOfWithDiscriminatorAndMappingSong
                    ?: fail("wrong response body")
                assertThat(safeBody.value.title).isEqualTo("puit")
                assertThat(safeBody.value.duration).isEqualTo(200)
                assertThat(safeBody.value.kind).isEqualTo("Song")
            }
    }

    @Test
    fun `sending option2 is accepted by endpoint with discriminator and mapping (Raw)`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "puit",
                    "duration": 200,
                    "kind": "Song" 
                }""".trimIndent()
            )
            .post("/features/oneOf/withDiscriminatorAndMapping")
            .execute()
            .statusCode(200)
            .body("title", equalTo("puit"))
            .body("duration", equalTo(200))
            .body("kind", equalTo("Song"))
    }

}