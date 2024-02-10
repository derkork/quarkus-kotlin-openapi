package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName

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

}

object Misc {

    val ObjectMapperClass = "ObjectMapper".rawClassName("com.fasterxml.jackson.databind")
    val RestResponseClass = "RestResponse".rawClassName("org.jboss.resteasy.reactive")
    val ResponseBuilderClass = "RestResponse.ResponseBuilder".rawClassName("org.jboss.resteasy.reactive")

}

object Library {

    val AllClasses = "*".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val MaybeClass = "Maybe".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")
    val MaybeSuccessClass = "Maybe.Success".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi")

}