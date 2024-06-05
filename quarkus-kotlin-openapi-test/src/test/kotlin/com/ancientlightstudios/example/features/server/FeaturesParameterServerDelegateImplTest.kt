package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.ApiTestBase
import com.ancientlightstudios.example.features.testclient.FeaturesParametersTestClient
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@QuarkusTest
class FeaturesParameterServerDelegateImplTest : ApiTestBase() {

    @Inject
    lateinit var objectMapper: ObjectMapper

    val client: FeaturesParametersTestClient
        get() = FeaturesParametersTestClient(objectMapper) { prepareRequestNew() }

    @Test
    fun `path parameters work`() {
        client.parametersPathSafe("foo", 10)
            .isOkResponse {
                assertThat(safeBody.name).isEqualTo("foo")
                assertThat(safeBody.id).isEqualTo(10)
            }
    }

    @Test
    fun `missing required parameters are rejected`() {
        client.parametersRequiredNotNullRaw {
            this
        }
            .isBadRequestResponse {
                assertThat(safeBody.messages).anySatisfy { assertThat(it).contains("querySingleValue") }
                assertThat(safeBody.messages).anySatisfy { assertThat(it).contains("headerSingleValue") }
                assertThat(safeBody.messages).anySatisfy { assertThat(it).contains("cookieSingleValue") }
            }
    }


//    @Test
//    fun `path parameters work`() {
//        prepareRequestNew()
//            .parametersPathSafe("foo", 10)
//
//            .isOkResponse {
//                fooHeader.containsExactly()
//                fooHeader { assertThat(this).containsExactly() }
//                header("fooHeader")
//                assertThat(fooHeader).isEqualsTo("narf")
//                assertThat(body.foo).isEquals()
//            }
//
//
//
//
//            .statusCode(200)
//            .withJsonBody {
//                assertTrue { it.getString("name") == "foo" }
//                assertTrue { it.getInt("id") == 10 }
//            }
//    }
//
//    @Test
//    fun `not sending the required parameter is rejected`() {
//        prepareRequest()
//            .log().all()
//            .log.ifValidationFails()
//            .get("/features/parameters/requiredNotNull".toTestUrl())
//
//            .execute()
//            .statusCode(400)
//            .header("fooHeader", object : BaseMatcher<Any>() {
//                override fun matches(actual: Any?): Boolean {
//
//                }
//
//                override fun describeTo(description: Description?) {
//
//                }
//            })
//    }

//    @Test
//    fun `test`() {
//        prepareRequest()
//            .queryParam("querySingleValue", "queryParam")
//            .queryParam("queryCollectionValue", listOf("queryParamItem1", "queryParamItem2"))
//            .header("headerSingleValue", "headerParam")
//            .headers(mapOf("headerCollectionValue" to listOf("headerParamItem1", "headerParamItem2")))
//            .cookies(mapOf("cookieSingleValue" to "cookieParam"))
//            .cookie("cookieCollectionValue", "cookieParamItem1")
//            .get("/features/parameters/requiredNotNull".toTestUrl())
//            .execute()
//            .statusCode(200)
//            .withStringBody {
//                it
//            }
//    }

//    @Test
//    fun `not sending the optional parameter is accepted`() {
//        val result = prepareRequest()
//            .log().ifValidationFails()
//            .queryParam("first", "foo")
//            .get("/features/parameters/test1".toTestUrl())
//            .execute()
//            .statusCode(204)
//            .header("X-FIRST-HEADER", "foo")
//
//
//
//    }
//
//    @Test
//    fun `sending the optional parameter is accepted`() {
//        prepareRequest()
//            .queryParam("first", "foo")
//            .queryParam("second", 10)
//            .get("/features/parameters/test1".toTestUrl())
//            .execute()
//            .statusCode(204)
//            .header("X-FIRST-HEADER", "foo")
//            .header("X-SECOND-HEADER", "10")
//    }
//
//    @Test
//    fun `sending the wrong parameter value is rejected`() {
//        prepareRequest()
//            .queryParam("first", "foo")
//            .queryParam("second", "foobar")
//            .get("/features/parameters/test1".toTestUrl())
//            .execute()
//            .statusCode(400)
//    }

}