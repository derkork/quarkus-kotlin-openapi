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
                    "name": "foo",
                    "status": "first",
                    "nameRequired": "puit",
                    "statusRequired": "first",
                    "itemsRequired": []
                }""".trimMargin()
            )
            .post("/features/oneOf/test1".toTestUrl())
            .execute()
            .statusCode(400)
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
                    "name": "foo",
                    "status": "first"
            }""".trimIndent()
            )
            .post("/features/oneOf/test1".toTestUrl())
            .execute()
            .statusCode(200)
            .withJsonBody {
                assertThat(it.getString("name")).isEqualTo("foo")
                assertThat(it.getString("status")).isEqualTo("first")
            }

    }

    @Test
    fun `sending valid option2 value is accepted`() {
        prepareRequest()
            .contentType("application/json")
            .body(
                """{
                    "nameRequired": "puit",
                    "statusRequired": "first",
                    "itemsRequired": []
            }""".trimIndent()
            )
            .post("/features/oneOf/test1".toTestUrl())
            .execute()
            .statusCode(200)
            .withJsonBody {
                assertThat(it.getString("nameRequired")).isEqualTo("puit")
                assertThat(it.getString("statusRequired")).isEqualTo("first")
                assertThat(it.getList<String>("itemsRequired")).isEmpty()
            }

    }
}
