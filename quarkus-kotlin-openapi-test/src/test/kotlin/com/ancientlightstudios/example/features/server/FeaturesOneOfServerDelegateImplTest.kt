package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.ApiTestBase
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@QuarkusTest
class FeaturesOneOfServerDelegateImplTest : ApiTestBase() {

    @Test
    fun `sending the wrong value is rejected`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                "foo": "bar"
                }""".trimMargin()
            )
            .post("/features/oneOf/test1".toTestUrl())
            .execute()
            .statusCode(400)
    }

    @Test
    fun `sending an object matching all options is rejected`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "foo",
                    "pages": 10,
                    "duration": 200,
                    "kind": "all"
                }""".trimMargin()
            )
            .post("/features/oneOf/test1".toTestUrl())
            .execute()
            .statusCode(400)
            .withStringBody {
                assertThat(it).contains("ambiguous")
            }
    }

    @Test
    fun `sending null is accepted because one options is nullable`() {
        prepareRequest()
            .contentType("application/json")
            .body("null")
            .post("/features/oneOf/test1".toTestUrl())
            .execute()
            .statusCode(200)
            .withStringBody {
                assertThat(it).isEqualTo("null")
            }
    }

    @Test
    fun `sending valid option1 value is accepted`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "foo",
                    "pages": 10,
                    "kind": "book"
            }""".trimIndent()
            )
            .post("/features/oneOf/test1".toTestUrl())
            .execute()
            .statusCode(200)
            .withJsonBody {
                assertThat(it.getString("title")).isEqualTo("foo")
                assertThat(it.getInt("pages")).isEqualTo(10)
                assertThat(it.getString("kind")).isEqualTo("book")
            }

    }

    @Test
    fun `sending valid option2 value is accepted`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "title": "puit",
                    "duration": 200,
                    "kind": "song"
            }""".trimIndent()
            )
            .post("/features/oneOf/test1".toTestUrl())
            .execute()
            .statusCode(200)
            .withJsonBody {
                assertThat(it.getString("title")).isEqualTo("puit")
                assertThat(it.getInt("duration")).isEqualTo(200)
                assertThat(it.getString("kind")).isEqualTo("song")
            }

    }
}
