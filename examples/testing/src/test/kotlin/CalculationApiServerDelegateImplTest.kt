import com.ancientlightstudios.example.testing.client.CalculationApiTestClient
import com.ancientlightstudios.example.testing.client.model.SumBody
import com.ancientlightstudios.quarkus.kotlin.openapi.UnsafeJson
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

@QuarkusTest
class CalculationApiServerDelegateImplTest {
    @TestHTTPResource("")
    lateinit var serverUrl: URL

    @Inject
    lateinit var objectMapper: ObjectMapper

    fun testClient() = CalculationApiTestClient(objectMapper) {
        RestAssured.given().baseUri(serverUrl.toString())
    }


    @Test
    fun `addition of two numbers works`() {
        // The `Safe` variant only allows calls that are structurally valid with proper data types
        testClient().sumSafe(SumBody(1, 2))
            // this will do a full validation of the response against the OpenAPI schema, we don't
            // need to do this ourselves.
            .isOkResponse {
                // the only thing we need to do is to validate the functional result (e.g. 1+2 == 3)
                assertThat(safeBody.result).isEqualTo(5)
            }
    }

    @Test
    fun `addition of two numbers fails if one is missing`() {
        // The `Unsafe` variant allows violating the API contract, e.g. setting properties to `null` that are not nullable.
        testClient().sumUnsafe {
            body(SumBody.unsafeJson(2, null))
        }
            .isBadRequestResponse {
                assertThat(safeBody.message).isEqualTo("request.body.b : is required")
            }
    }

    @Test
    fun `addition of two numbers fails if one is a string`() {
        // The `Raw` variant allows us to freely manipulate the request body,
        // e.g. to test how the server behaves when it receives invalid data.
        testClient().sumRaw {
            val brokenBody = SumBody.unsafeJson(2, null).value as ObjectNode
            brokenBody.put("b", "Not a number")
            body(brokenBody.toString())
        }
            .isBadRequestResponse {
                assertThat(safeBody.message).isEqualTo("request.body.b : is not an int")
            }
    }

    @Test
    fun `addition fails if we send something different than JSON`() {
        testClient().sumRaw {
            body("This is not JSON")
        }.isBadRequestResponse {
            assertThat(safeBody.message).isEqualTo("request.body : is not valid json")
        }
    }

}