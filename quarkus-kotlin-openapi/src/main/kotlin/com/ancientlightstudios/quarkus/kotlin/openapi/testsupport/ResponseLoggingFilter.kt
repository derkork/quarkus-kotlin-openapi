package com.ancientlightstudios.quarkus.kotlin.openapi.testsupport

import io.restassured.builder.ResponseBuilder
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.filter.log.LogDetail
import io.restassured.internal.RestAssuredResponseImpl
import io.restassured.internal.print.ResponsePrinter
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ResponseLoggingFilter(private val stream: PrintStream) : Filter {
    override fun filter(
        requestSpec: FilterableRequestSpecification, responseSpec: FilterableResponseSpecification, ctx: FilterContext
    ): Response {

        val response = ctx.next(requestSpec, responseSpec)

        ResponsePrinter.print(response, response, stream, LogDetail.STATUS, true, setOf())
        ResponsePrinter.print(response, response, stream, LogDetail.HEADERS, true, setOf())

        val body = response.asByteArray()
        val baos = ByteArrayOutputStream()
        val bodyStream = PrintStream(baos)

        ResponsePrinter.print(response, response, bodyStream, LogDetail.BODY, true, setOf())

        if (baos.size() > 5000) {
            // split and only print first 2500 and last 2500 bytes
            val bytes = baos.toByteArray()
            stream.writeBytes(bytes.take(2500).toByteArray())
            stream.print(" ... ")
            stream.writeBytes(bytes.takeLast(2500).toByteArray())
        } else {
            stream.writeBytes(baos.toByteArray())
        }

        return cloneResponseIfNeeded(response, body)
    }

    /**
     * If body expectations are defined we need to return a new Response otherwise the stream
     * has been closed due to the logging.
     */
    private fun cloneResponseIfNeeded(response: Response, responseAsString: ByteArray?): Response {
        if (responseAsString != null && response is RestAssuredResponseImpl && !response.hasExpectations) {
            val build = ResponseBuilder().clone(response).setBody(responseAsString).build()
            (build as RestAssuredResponseImpl).hasExpectations = true
            return build
        }
        return response
    }

}