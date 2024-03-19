package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName

object Kotlin {

    val Star = "*".rawClassName("", true)
    val AnyClass = "Any".rawClassName("kotlin", true)
    val ByteArrayClass = "ByteArray".rawClassName("kotlin", true)
    val StringClass = "String".rawClassName("kotlin", true)
    val BooleanClass = "Boolean".rawClassName("kotlin", true)
    val FloatClass = "Float".rawClassName("kotlin", true)
    val DoubleClass = "Double".rawClassName("kotlin", true)
    val IntClass = "Int".rawClassName("kotlin", true)
    val LongClass = "Long".rawClassName("kotlin", true)
    val UIntClass = "UInt".rawClassName("kotlin", true)
    val ULongClass = "ULong".rawClassName("kotlin", true)
    val ListClass = "List".rawClassName("kotlin.collections", true)
    val ExceptionClass = "Exception".rawClassName("kotlin", true)
    val JvmNameClass = "JvmName".rawClassName("kotlin.jvm", true)
    val PairClass = "Pair".rawClassName("kotlin", true)
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

}

object Misc {

    val ObjectMapperClass = "ObjectMapper".rawClassName("com.fasterxml.jackson.databind")
    val RestResponseClass = "RestResponse".rawClassName("org.jboss.resteasy.reactive")
    val RestResponseStatusClass = RestResponseClass.rawNested("Status")
    val ResponseBuilderClass = RestResponseClass.rawNested("ResponseBuilder")
    val JsonNodeClass = "JsonNode".rawClassName("com.fasterxml.jackson.databind")
    val RegisterRestClientClass = "RegisterRestClient".rawClassName("org.eclipse.microprofile.rest.client.inject")
    val RestClientClass = "RestClient".rawClassName("org.eclipse.microprofile.rest.client.inject")
    val TimeoutExceptionClass = "TimeoutException".rawClassName("java.util.concurrent")

}

object Library {

    val AllClasses = "*".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val MaybeClass = "Maybe".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val MaybeSuccessClass = MaybeClass.rawNested("Success")
    val MaybeFailureClass = MaybeClass.rawNested("Failure")
    val ValidationErrorClass = "ValidationError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val IsErrorClass = "IsError".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val DefaultValidatorClass = "DefaultValidator".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")

}