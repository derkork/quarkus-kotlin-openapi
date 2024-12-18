package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName

object Kotlin {

    val Star = "*".rawClassName("", true)
    val UnitType = "Unit".rawClassName("kotlin", true).typeName()
    val NothingType = "Nothing".rawClassName("kotlin", true).typeName()
    val AnyClass = "Any".rawClassName("kotlin", true)
    val ByteArrayClass = "ByteArray".rawClassName("kotlin", true)
    val StringClass = "String".rawClassName("kotlin", true)
    val BooleanClass = "Boolean".rawClassName("kotlin", true)
    val FloatClass = "Float".rawClassName("kotlin", true)
    val DoubleClass = "Double".rawClassName("kotlin", true)
    val BigDecimalClass = "BigDecimal".rawClassName("java.math")
    val IntClass = "Int".rawClassName("kotlin", true)
    val LongClass = "Long".rawClassName("kotlin", true)
    val UIntClass = "UInt".rawClassName("kotlin", true)
    val ULongClass = "ULong".rawClassName("kotlin", true)
    val BigIntegerClass = "BigInteger".rawClassName("java.math")
    val ListClass = "List".rawClassName("kotlin.collections", true)
    val MapClass = "Map".rawClassName("kotlin.collections", true)
    val ExceptionClass = "Exception".rawClassName("kotlin", true)
    val ThrowableClass = "Throwable".rawClassName("kotlin", true)
    val JvmNameClass = "JvmName".rawClassName("kotlin.jvm", true)
    val PairClass = "Pair".rawClassName("kotlin", true)
    val IllegalStateExceptionClass = "IllegalStateException".rawClassName("kotlin", true)
    val ByteArrayOutputStreamClass = "ByteArrayOutputStream".rawClassName("java.io")
    val PrintStreamClass = "PrintStream".rawClassName("java.io")

}

object Jakarta {

    val PathAnnotationClass = "Path".rawClassName("jakarta.ws.rs")
    val GetAnnotationClass = "GET".rawClassName("jakarta.ws.rs")
    val PutAnnotationClass = "PUT".rawClassName("jakarta.ws.rs")
    val PostAnnotationClass = "POST".rawClassName("jakarta.ws.rs")
    val DeleteAnnotationClass = "DELETE".rawClassName("jakarta.ws.rs")
    val OptionsAnnotationClass = "OPTIONS".rawClassName("jakarta.ws.rs")
    val HeadAnnotationClass = "HEAD".rawClassName("jakarta.ws.rs")
    val PatchAnnotationClass = "PATCH".rawClassName("jakarta.ws.rs")
    val TraceAnnotationClass = "TRACE".rawClassName("jakarta.ws.rs")
    val ConsumesAnnotationClass = "Consumes".rawClassName("jakarta.ws.rs")
    val PathParamAnnotationClass = "PathParam".rawClassName("jakarta.ws.rs")
    val QueryParamAnnotationClass = "QueryParam".rawClassName("jakarta.ws.rs")
    val HeaderParamAnnotationClass = "HeaderParam".rawClassName("jakarta.ws.rs")
    val CookieParamAnnotationClass = "CookieParam".rawClassName("jakarta.ws.rs")
    val FormParamAnnotationClass = "FormParam".rawClassName("jakarta.ws.rs")
    val WebApplicationExceptionClass = "WebApplicationException".rawClassName("jakarta.ws.rs")
    val ResponseClass = "Response".className("jakarta.ws.rs.core")
    val ApplicationScopedClass = "ApplicationScoped".rawClassName("jakarta.enterprise.context")
    val HttpHeadersClass = "HttpHeaders".rawClassName("jakarta.ws.rs.core")
    val ContextAnnotationClass = "Context".rawClassName("jakarta.ws.rs.core")

}

object RestAssured {

    val ResponseClass = "Response".className("io.restassured.response")
    val RequestSpecificationClass = "RequestSpecification".rawClassName("io.restassured.specification")

}

object Misc {

    val ObjectMapperClass = "ObjectMapper".rawClassName("com.fasterxml.jackson.databind")
    val NullNodeClass = "NullNode".rawClassName("com.fasterxml.jackson.databind.node")
    val RestResponseClass = "RestResponse".rawClassName("org.jboss.resteasy.reactive")
    val ResponseBuilderClass = RestResponseClass.rawNested("ResponseBuilder")
    val JsonNodeClass = "JsonNode".rawClassName("com.fasterxml.jackson.databind")
    val RegisterRestClientClass = "RegisterRestClient".rawClassName("org.eclipse.microprofile.rest.client.inject")
    val RestClientClass = "RestClient".rawClassName("org.eclipse.microprofile.rest.client.inject")
    val RegisterProviderClass = "RegisterProvider".rawClassName("org.eclipse.microprofile.rest.client.annotation")
    val TimeoutExceptionClass = "TimeoutException".rawClassName("java.util.concurrent")
    val AssertionFailedErrorClass = "AssertionFailedError".rawClassName("org.opentest4j")
    val LoggerFactoryClass = "LoggerFactory".rawClassName("org.slf4j")
    val LoggerClass = "Logger".rawClassName("org.slf4j")
    val MDCClass = "MDC".rawClassName("org.slf4j")
    val MDCContextClass = "MDCContext".rawClassName("kotlinx.coroutines.slf4j")
}

object Quarkus {

    val IfBuildProfileAnnotationClass = "IfBuildProfile".rawClassName("io.quarkus.arc.profile")
    val UnlessBuildProfileAnnotationClass = "UnlessBuildProfile".rawClassName("io.quarkus.arc.profile")

}

object Library {

    val AllClasses = "*".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val MaybeClass = "Maybe".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val MaybeSuccessClass = MaybeClass.rawNested("Success")
    val MaybeFailureClass = MaybeClass.rawNested("Failure")
    val ValidationErrorClass = "ValidationError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val IsErrorClass = "IsError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val IsTimeoutErrorClass = "IsTimeoutError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val IsUnreachableErrorClass = "IsUnreachableError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val IsConnectionResetErrorClass = "IsConnectionResetError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val IsUnknownErrorClass = "IsUnknownError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val IsResponseErrorClass = "IsResponseError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val DefaultValidatorClass = "DefaultValidator".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val RequestHandledSignalClass = "RequestHandledSignal".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val ResponseWithGenericStatusInterface = "ResponseWithGenericStatus".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val RequestContextInterface = "RequestContext".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val PropertiesContainerInterface = "PropertiesContainer".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val UnsafeJsonClass = "UnsafeJson".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val RequestLoggingFilterClass = "RequestLoggingFilter".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi.testsupport")
    val ResponseLoggingFilterClass = "ResponseLoggingFilter".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi.testsupport")

}