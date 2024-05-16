package com.ancientlightstudios.example.features.client

import com.ancientlightstudios.example.features.client.model.*
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@QuarkusTest
class FeaturesOneOfClientTest {

    @Inject
    lateinit var client: FeaturesOneOfClient

    @Test
    fun `sending option1 value is accepted`() {
        runBlocking {
            when (val response = client.oneOfTest1(
                OneOfWithoutDiscriminatorSimpleForm(
                    SimpleForm("foo", SimpleEnum.First)
                )
            )) {
                is OneOfTest1HttpResponse.Ok -> {
                    val safeBody =
                        response.safeBody as? OneOfWithoutDiscriminatorSimpleForm ?: fail("wrong response body")
                    assertThat(safeBody.value.name).isEqualTo("foo")
                    assertThat(safeBody.value.status).isEqualTo(SimpleEnum.First)
                }

                is OneOfTest1HttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `sending option2 value is accepted`() {
        runBlocking {
            when (val response = client.oneOfTest1(
                OneOfWithoutDiscriminatorSimpleObjectOptional(
                    SimpleObject(nameRequired = "puit", statusRequired = SimpleEnum.First, itemsRequired = emptyList())
                )
            )) {
                is OneOfTest1HttpResponse.Ok -> {
                    val safeBody =
                        response.safeBody as? OneOfWithoutDiscriminatorSimpleObjectOptional
                            ?: fail("wrong response body")
                    assertThat(safeBody.value).isNotNull
                    assertThat(safeBody.value!!.nameRequired).isEqualTo("puit")
                    assertThat(safeBody.value!!.statusRequired).isEqualTo(SimpleEnum.First)
                    assertThat(safeBody.value!!.itemsRequired).isEmpty()
                }

                is OneOfTest1HttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

    @Test
    fun `sending null as option2 value is accepted`() {
        runBlocking {
            when (val response = client.oneOfTest1(
                OneOfWithoutDiscriminatorSimpleObjectOptional(null)
            )) {
                is OneOfTest1HttpResponse.Ok -> {
                    val safeBody =
                        response.safeBody as? OneOfWithoutDiscriminatorSimpleObjectOptional
                            ?: fail("wrong response body")
                    assertThat(safeBody.value).isNull()
                }

                is OneOfTest1HttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

}