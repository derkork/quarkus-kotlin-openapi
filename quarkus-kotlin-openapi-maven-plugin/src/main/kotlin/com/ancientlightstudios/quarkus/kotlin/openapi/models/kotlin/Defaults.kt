package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

object Kotlin {

    val Star = KotlinTypeReference("*", "")
    val Any = KotlinTypeReference("Any", "kotlin")
    val Unit = KotlinTypeReference("Unit", "kotlin")
    val Nothing = KotlinTypeReference("Nothing", "kotlin")
    val ByteArray = KotlinTypeReference("ByteArray", "kotlin")
    val String = KotlinTypeReference("String", "kotlin")
    val Boolean = KotlinTypeReference("Boolean", "kotlin")
    val Float = KotlinTypeReference("Float", "kotlin")
    val Double = KotlinTypeReference("Double", "kotlin")
    val BigDecimal = KotlinTypeReference("BigDecimal", "java.math")
    val Int = KotlinTypeReference("Int", "kotlin")
    val Long = KotlinTypeReference("Long", "kotlin")
    val UInt = KotlinTypeReference("UInt", "kotlin")
    val ULong = KotlinTypeReference("ULong", "kotlin")
    val BigInteger = KotlinTypeReference("BigInteger", "java.math")
    val List = KotlinTypeReference("List", "kotlin.collections")
    val Map = KotlinTypeReference("Map", "kotlin.collections")
    val Exception = KotlinTypeReference("Exception", "kotlin")
    val Throwable = KotlinTypeReference("Throwable", "kotlin")
    val JvmName = KotlinTypeReference("JvmName", "kotlin.jvm")
    val Pair = KotlinTypeReference("Pair", "kotlin")
    val IllegalStateException = KotlinTypeReference("IllegalStateException", "kotlin")
    val ByteArrayOutputStream = KotlinTypeReference("ByteArrayOutputStream", "java.io")
    val PrintStream = KotlinTypeReference("PrintStream", "java.io")

}

object Jakarta {

    val PathAnnotation = KotlinTypeReference("Path", "jakarta.ws.rs")
    val GetAnnotation = KotlinTypeReference("GET", "jakarta.ws.rs")
    val PutAnnotation = KotlinTypeReference("PUT", "jakarta.ws.rs")
    val PostAnnotation = KotlinTypeReference("POST", "jakarta.ws.rs")
    val DeleteAnnotation = KotlinTypeReference("DELETE", "jakarta.ws.rs")
    val OptionsAnnotation = KotlinTypeReference("OPTIONS", "jakarta.ws.rs")
    val HeadAnnotation = KotlinTypeReference("HEAD", "jakarta.ws.rs")
    val PatchAnnotation = KotlinTypeReference("PATCH", "jakarta.ws.rs")
    val TraceAnnotation = KotlinTypeReference("TRACE", "jakarta.ws.rs")
    val ConsumesAnnotation = KotlinTypeReference("Consumes", "jakarta.ws.rs")
    val PathParamAnnotation = KotlinTypeReference("PathParam", "jakarta.ws.rs")
    val QueryParamAnnotation = KotlinTypeReference("QueryParam", "jakarta.ws.rs")
    val HeaderParamAnnotation = KotlinTypeReference("HeaderParam", "jakarta.ws.rs")
    val CookieParamAnnotation = KotlinTypeReference("CookieParam", "jakarta.ws.rs")
    val FormParamAnnotation = KotlinTypeReference("FormParam", "jakarta.ws.rs")
    val WebApplicationException = KotlinTypeReference("WebApplicationException", "jakarta.ws.rs")
    val Response = KotlinTypeReference("Response", "jakarta.ws.rs.core")
    val ApplicationScoped = KotlinTypeReference("ApplicationScoped", "jakarta.enterprise.context")
    val HttpHeaders = KotlinTypeReference("HttpHeaders", "jakarta.ws.rs.core")
    val ContextAnnotation = KotlinTypeReference("Context", "jakarta.ws.rs.core")

}

object RestAssured {

    val Response = KotlinTypeReference("Response", "io.restassured.response")
    val RequestSpecification = KotlinTypeReference("RequestSpecification", "io.restassured.specification")

}

object Misc {

    val ObjectMapper = KotlinTypeReference("ObjectMapper", "com.fasterxml.jackson.databind")
    val NullNode = KotlinTypeReference("NullNode", "com.fasterxml.jackson.databind.node")
    val RestResponse = KotlinTypeReference("RestResponse", "org.jboss.resteasy.reactive")

    //    val ResponseBuilder = RestResponse.rawNested("ResponseBuilder")

    val JsonNode = KotlinTypeReference("JsonNode", "com.fasterxml.jackson.databind")
    val RegisterRestClient = KotlinTypeReference("RegisterRestClient", "org.eclipse.microprofile.rest.client.inject")
    val RestClient = KotlinTypeReference("RestClient", "org.eclipse.microprofile.rest.client.inject")
    val RegisterProvider = KotlinTypeReference("RegisterProvider", "org.eclipse.microprofile.rest.client.annotation")
    val TimeoutException = KotlinTypeReference("TimeoutException", "java.util.concurrent")
    val AssertionFailedError = KotlinTypeReference("AssertionFailedError", "org.opentest4j")
    val LoggerFactory = KotlinTypeReference("LoggerFactory", "org.slf4j")
    val Logger = KotlinTypeReference("Logger", "org.slf4j")
    val MDC = KotlinTypeReference("MDC", "org.slf4j")
    val MDCContext = KotlinTypeReference("MDCContext", "kotlinx.coroutines.slf4j")

}

object Quarkus {

    val IfBuildProfileAnnotation = KotlinTypeReference("IfBuildProfile", "io.quarkus.arc.profile")
    val UnlessBuildProfileAnnotation = KotlinTypeReference("UnlessBuildProfile", "io.quarkus.arc.profile")

}

object Library {

    private val packageName = "com.ancientlightstudios.quarkus.kotlin.openapi"

    val All = KotlinTypeReference("*", packageName)
    val Maybe = KotlinTypeReference("Maybe", packageName)

    //    val MaybeSuccessClass = MaybeClass.rawNested("Success")
    //    val MaybeFailureClass = MaybeClass.rawNested("Failure")

    val ValidationError = KotlinTypeReference("ValidationError", packageName)
    val IsError = KotlinTypeReference("IsError", packageName)
    val IsTimeoutError = KotlinTypeReference("IsTimeoutError", packageName)
    val IsUnreachableError = KotlinTypeReference("IsUnreachableError", packageName)
    val IsConnectionResetError = KotlinTypeReference("IsConnectionResetError", packageName)
    val IsUnknownError = KotlinTypeReference("IsUnknownError", packageName)
    val IsResponseError = KotlinTypeReference("IsResponseError", packageName)
    val DefaultValidator = KotlinTypeReference("DefaultValidator", packageName)
    val RequestHandledSignal = KotlinTypeReference("RequestHandledSignal", packageName)
    val ResponseWithGenericStatus = KotlinTypeReference("ResponseWithGenericStatus", packageName)
    val RequestContext = KotlinTypeReference("RequestContext", packageName)
    val PropertiesContainer = KotlinTypeReference("PropertiesContainer", packageName)
    val UnsafeJson = KotlinTypeReference("UnsafeJson", packageName)
    val RequestLoggingFilter = KotlinTypeReference("RequestLoggingFilter", "$packageName.testsupport")
    val ResponseLoggingFilter = KotlinTypeReference("ResponseLoggingFilter", "$packageName.testsupport")

}