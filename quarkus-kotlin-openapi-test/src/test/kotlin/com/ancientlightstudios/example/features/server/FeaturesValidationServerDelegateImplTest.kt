package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.ApiTestBase
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@QuarkusTest
class FeaturesValidationServerDelegateImplTest : ApiTestBase() {

//    @ParameterizedTest
//    @ValueSource(longs = [4, 11])
//    fun `invalid long numbers are rejected`(value: Long) {
//        prepareRequest()
//            .contentType("application/json")
//            .body("""{
//                "longProperty": $value,
//                "uintProperty": 6
//                }""".trimIndent())
//            .post("/features/validation/numberValidation".toTestUrl())
//            .execute()
//            .statusCode(400)
//            .withJsonBody {
//                assertThat(it.getString("messages")).contains("longProperty")
//                assertThat(it.getString("messages")).doesNotContain("uintProperty")
//            }
//    }
//
//    @ParameterizedTest
//    @ValueSource(ints = [5, 10])
//    fun `invalid uint numbers are rejected`(value: Int) {
//        prepareRequest()
//            .contentType("application/json")
//            .body("""{
//                "longProperty": 6,
//                "uintProperty": $value
//                }""".trimIndent())
//            .post("/features/validation/numberValidation".toTestUrl())
//            .execute()
//            .statusCode(400)
//            .withJsonBody {
//                assertThat(it.getString("messages")).contains("uintProperty")
//                assertThat(it.getString("messages")).doesNotContain("longProperty")
//            }
//    }
//
//    @Test
//    fun `valid minimum is accepted`() {
//        prepareRequest()
//            .contentType("application/json")
//            .body("""{
//                "longProperty": 5,
//                "uintProperty": 6
//                }""".trimIndent())
//            .post("/features/validation/numberValidation".toTestUrl())
//            .execute()
//            .statusCode(200)
//    }
//
//    @Test
//    fun `valid maximum is accepted`() {
//        prepareRequest()
//            .contentType("application/json")
//            .body("""{
//                "longProperty": 10,
//                "uintProperty": 9
//                }""".trimIndent())
//            .post("/features/validation/numberValidation".toTestUrl())
//            .execute()
//            .statusCode(200)
//    }


}