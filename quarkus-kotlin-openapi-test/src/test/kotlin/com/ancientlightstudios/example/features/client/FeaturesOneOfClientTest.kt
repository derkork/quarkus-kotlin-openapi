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
                OneOfWithoutDiscriminatorBook(
                    Book("foo", 10, "book")
                )
            )) {
                is OneOfTest1HttpResponse.Ok -> {
                    val safeBody =
                        response.safeBody as? OneOfWithoutDiscriminatorBook ?: fail("wrong response body")
                    assertThat(safeBody.value.title).isEqualTo("foo")
                    assertThat(safeBody.value.pages).isEqualTo(10)
                    assertThat(safeBody.value.kind).isEqualTo("book")
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
                OneOfWithoutDiscriminatorSong(
                    Song("puit", 200, "song")
                )
            )) {
                is OneOfTest1HttpResponse.Ok -> {
                    val safeBody =
                        response.safeBody as? OneOfWithoutDiscriminatorSong
                            ?: fail("wrong response body")
                    assertThat(safeBody.value).isNotNull
                    assertThat(safeBody.value!!.title).isEqualTo("puit")
                    assertThat(safeBody.value!!.duration).isEqualTo(200)
                    assertThat(safeBody.value!!.kind).isEqualTo("song")
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
                OneOfWithoutDiscriminatorSong(null)
            )) {
                is OneOfTest1HttpResponse.Ok -> {
                    val safeBody =
                        response.safeBody as? OneOfWithoutDiscriminatorSong
                            ?: fail("wrong response body")
                    assertThat(safeBody.value).isNull()
                }

                is OneOfTest1HttpResponse -> fail("received status code ${response.status}")
                else -> fail("request failed")
            }
        }
    }

}