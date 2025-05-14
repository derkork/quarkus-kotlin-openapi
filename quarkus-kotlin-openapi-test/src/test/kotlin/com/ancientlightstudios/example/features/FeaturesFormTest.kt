package com.ancientlightstudios.example.features

import com.ancientlightstudios.example.features.client.FeaturesFormClient
import com.ancientlightstudios.example.features.client.FormObjectHttpResponse
import com.ancientlightstudios.example.features.client.model.FormEnum
import com.ancientlightstudios.example.features.client.model.SimpleForm
import com.ancientlightstudios.example.features.testclient.FeaturesFormTestClient
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import com.ancientlightstudios.example.features.testclient.model.FormEnum as TestFormEnum
import com.ancientlightstudios.example.features.testclient.model.SimpleForm as TestSimpleForm

@QuarkusTest
class FeaturesFormTest : ApiTestBase() {

    @Inject
    lateinit var client: FeaturesFormClient

    val testClient: FeaturesFormTestClient
        get() = FeaturesFormTestClient(dependencyContainer) { prepareRequest() }

    @Test
    fun `sending a value is accepted (Client)`() {
        runBlocking {
            val response = client.formObject(
                SimpleForm("foo", FormEnum.Second)
            )
            if (response is FormObjectHttpResponse.Ok) {
                assertThat(response.safeBody.name).isEqualTo("foo")
                assertThat(response.safeBody.status).isEqualTo(FormEnum.Second)
            } else {
                fail("unexpected response")
            }
        }
    }

    @Test
    fun `sending a value is accepted (Test-Client)`() {
        testClient.formObjectSafe(TestSimpleForm("foo", TestFormEnum.Second))
            .isOkResponse {
                assertThat(safeBody.name).isEqualTo("foo")
                assertThat(safeBody.status).isEqualTo(TestFormEnum.Second)
            }
    }

    @Test
    fun `sending a value is accepted (Raw)`() {
        prepareRequest()
            .formParam("name", "foo")
            .formParam("status", FormEnum.Second.value)
            .contentType("application/x-www-form-urlencoded")
            .post("/features/form/object")
            .execute()
            .statusCode(200)
            .body("name", equalTo("foo"))
            .body("status", equalTo(FormEnum.Second.value))
    }
}