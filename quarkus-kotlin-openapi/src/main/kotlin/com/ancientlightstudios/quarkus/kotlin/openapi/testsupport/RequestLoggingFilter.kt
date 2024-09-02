package com.ancientlightstudios.quarkus.kotlin.openapi.testsupport

import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.filter.log.LogDetail
import io.restassured.filter.log.UrlDecoder
import io.restassured.internal.print.RequestPrinter
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset


class RequestLoggingFilter(private val stream: PrintStream) : Filter {

    override fun filter(
        requestSpec: FilterableRequestSpecification,
        responseSpec: FilterableResponseSpecification,
        ctx: FilterContext
    ): Response {
        val uri = UrlDecoder.urlDecode(
            requestSpec.uri,
            Charset.forName(requestSpec.config.encoderConfig.defaultQueryParameterCharset()),
            true
        )

        RequestPrinter.print(requestSpec, requestSpec.method, uri,
            LogDetail.values()
                .filter { it != LogDetail.ALL && it != LogDetail.BODY }
                .toSet(),
            setOf(), stream, true
        )

        val baos = ByteArrayOutputStream()
        val bodyStream = PrintStream(baos)
        RequestPrinter.print(requestSpec, requestSpec.method, uri, LogDetail.BODY, setOf(), bodyStream, true)

        if (baos.size() > 5000) {
            // split and only print first 2500 and last 2500 bytes
            val bytes = baos.toByteArray()
            stream.writeBytes(bytes.take(2500).toByteArray())
            stream.print(" ... ")
            stream.writeBytes(bytes.takeLast(2500).toByteArray())
        }
        else {
            stream.writeBytes(baos.toByteArray())
        }

        return ctx.next(requestSpec, responseSpec)
    }

}