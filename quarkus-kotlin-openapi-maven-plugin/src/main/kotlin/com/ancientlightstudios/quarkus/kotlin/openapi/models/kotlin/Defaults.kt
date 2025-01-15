package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.nestedTypeName

object Kotlin {

    val Star = KotlinTypeName("*", "")
    val Any = KotlinTypeName("Any", "kotlin")
    val Unit = KotlinTypeName("Unit", "kotlin")
    val Nothing = KotlinTypeName("Nothing", "kotlin")
    val ByteArray = KotlinTypeName("ByteArray", "kotlin")
    val String = KotlinTypeName("String", "kotlin")
    val Boolean = KotlinTypeName("Boolean", "kotlin")
    val Float = KotlinTypeName("Float", "kotlin")
    val Double = KotlinTypeName("Double", "kotlin")
    val BigDecimal = KotlinTypeName("BigDecimal", "java.math")
    val Int = KotlinTypeName("Int", "kotlin")
    val Long = KotlinTypeName("Long", "kotlin")
    val UInt = KotlinTypeName("UInt", "kotlin")
    val ULong = KotlinTypeName("ULong", "kotlin")
    val BigInteger = KotlinTypeName("BigInteger", "java.math")
    val List = KotlinTypeName("List", "kotlin.collections")
    val Map = KotlinTypeName("Map", "kotlin.collections")
    val Exception = KotlinTypeName("Exception", "kotlin")
    val Throwable = KotlinTypeName("Throwable", "kotlin")
    val JvmName = KotlinTypeName("JvmName", "kotlin.jvm")
    val Pair = KotlinTypeName("Pair", "kotlin")
    val IllegalStateException = KotlinTypeName("IllegalStateException", "kotlin")
    val ByteArrayOutputStream = KotlinTypeName("ByteArrayOutputStream", "java.io")
    val PrintStream = KotlinTypeName("PrintStream", "java.io")

}

object Jakarta {

    val PathAnnotation = KotlinTypeName("Path", "jakarta.ws.rs")
    val GetAnnotation = KotlinTypeName("GET", "jakarta.ws.rs")
    val PutAnnotation = KotlinTypeName("PUT", "jakarta.ws.rs")
    val PostAnnotation = KotlinTypeName("POST", "jakarta.ws.rs")
    val DeleteAnnotation = KotlinTypeName("DELETE", "jakarta.ws.rs")
    val OptionsAnnotation = KotlinTypeName("OPTIONS", "jakarta.ws.rs")
    val HeadAnnotation = KotlinTypeName("HEAD", "jakarta.ws.rs")
    val PatchAnnotation = KotlinTypeName("PATCH", "jakarta.ws.rs")
    val TraceAnnotation = KotlinTypeName("TRACE", "jakarta.ws.rs")
    val ConsumesAnnotation = KotlinTypeName("Consumes", "jakarta.ws.rs")
    val PathParamAnnotation = KotlinTypeName("PathParam", "jakarta.ws.rs")
    val QueryParamAnnotation = KotlinTypeName("QueryParam", "jakarta.ws.rs")
    val HeaderParamAnnotation = KotlinTypeName("HeaderParam", "jakarta.ws.rs")
    val CookieParamAnnotation = KotlinTypeName("CookieParam", "jakarta.ws.rs")
    val FormParamAnnotation = KotlinTypeName("FormParam", "jakarta.ws.rs")
    val WebApplicationException = KotlinTypeName("WebApplicationException", "jakarta.ws.rs")
    val Response = KotlinTypeName("Response", "jakarta.ws.rs.core")
    val ApplicationScoped = KotlinTypeName("ApplicationScoped", "jakarta.enterprise.context")
    val HttpHeaders = KotlinTypeName("HttpHeaders", "jakarta.ws.rs.core")
    val ContextAnnotation = KotlinTypeName("Context", "jakarta.ws.rs.core")

}

object RestAssured {

    val Response = KotlinTypeName("Response", "io.restassured.response")
    val RequestSpecification = KotlinTypeName("RequestSpecification", "io.restassured.specification")

}

object Misc {

    val ObjectMapper = KotlinTypeName("ObjectMapper", "com.fasterxml.jackson.databind")
    val NullNode = KotlinTypeName("NullNode", "com.fasterxml.jackson.databind.node")
    val RestResponse = KotlinTypeName("RestResponse", "org.jboss.resteasy.reactive")
    val ResponseBuilder = RestResponse.nestedTypeName("ResponseBuilder")
    val JsonNode = KotlinTypeName("JsonNode", "com.fasterxml.jackson.databind")
    val RegisterRestClient = KotlinTypeName("RegisterRestClient", "org.eclipse.microprofile.rest.client.inject")
    val RestClient = KotlinTypeName("RestClient", "org.eclipse.microprofile.rest.client.inject")
    val RegisterProvider = KotlinTypeName("RegisterProvider", "org.eclipse.microprofile.rest.client.annotation")
    val TimeoutException = KotlinTypeName("TimeoutException", "java.util.concurrent")
    val AssertionFailedError = KotlinTypeName("AssertionFailedError", "org.opentest4j")
    val LoggerFactory = KotlinTypeName("LoggerFactory", "org.slf4j")
    val Logger = KotlinTypeName("Logger", "org.slf4j")
    val MDC = KotlinTypeName("MDC", "org.slf4j")
    val MDCContext = KotlinTypeName("MDCContext", "kotlinx.coroutines.slf4j")

}

object Quarkus {

    val IfBuildProfileAnnotation = KotlinTypeName("IfBuildProfile", "io.quarkus.arc.profile")
    val UnlessBuildProfileAnnotation = KotlinTypeName("UnlessBuildProfile", "io.quarkus.arc.profile")

}

object Library {

    private val packageName = "com.ancientlightstudios.quarkus.kotlin.openapi"

    val All = KotlinTypeName("*", packageName)
    val Maybe = KotlinTypeName("Maybe", packageName)
    val MaybeSuccess = Maybe.nestedTypeName("Success")
    val MaybeFailure = Maybe.nestedTypeName("Failure")
    val ValidationError = KotlinTypeName("ValidationError", packageName)
    val ErrorKind = KotlinTypeName("ErrorKind", packageName)
    val IsError = KotlinTypeName("IsError", packageName)
    val IsTimeoutError = KotlinTypeName("IsTimeoutError", packageName)
    val IsUnreachableError = KotlinTypeName("IsUnreachableError", packageName)
    val IsConnectionResetError = KotlinTypeName("IsConnectionResetError", packageName)
    val IsUnknownError = KotlinTypeName("IsUnknownError", packageName)
    val IsResponseError = KotlinTypeName("IsResponseError", packageName)
    val DefaultValidator = KotlinTypeName("DefaultValidator", packageName)
    val RequestHandledSignal = KotlinTypeName("RequestHandledSignal", packageName)
    val ResponseWithGenericStatus = KotlinTypeName("ResponseWithGenericStatus", packageName)
    val RequestContext = KotlinTypeName("RequestContext", packageName)
    val PropertiesContainer = KotlinTypeName("PropertiesContainer", packageName)
    val UnsafeJson = KotlinTypeName("UnsafeJson", packageName)
    val RequestLoggingFilter = KotlinTypeName("RequestLoggingFilter", "$packageName.testsupport")
    val ResponseLoggingFilter = KotlinTypeName("ResponseLoggingFilter", "$packageName.testsupport")

}