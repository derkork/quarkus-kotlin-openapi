package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ClassName private constructor(val packageName: String, val value: String, val provided: Boolean) {

    fun extend(prefix: String = "", postfix: String = "", packageName: String = this.packageName) =
        value.className(packageName, prefix, postfix)

    companion object {

        fun String.rawClassName(packageName: String, provided: Boolean) = ClassName(packageName, this, provided)

        fun String.className(packageName: String, prefix: String = "", postfix: String = "") =
            "$prefix $this $postfix".toKotlinIdentifier()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
                .let { ClassName(packageName, it, false) }

    }

}

object Kotlin {

    val Star = "*".rawClassName("", true)
    val StringClass = "String".rawClassName("kotlin", true)

}

object Misc {

    val PathAnnotationClass = "Path".rawClassName("jakarta.ws.rs", false)
    val GetAnnotationClass = "GET".rawClassName("jakarta.ws.rs", false)
    val PutAnnotationClass = "PUT".rawClassName("jakarta.ws.rs", false)
    val PostAnnotationClass = "POST".rawClassName("jakarta.ws.rs", false)
    val DeleteAnnotationClass = "DELETE".rawClassName("jakarta.ws.rs", false)
    val OptionsAnnotationClass = "OPTIONS".rawClassName("jakarta.ws.rs", false)
    val HeadAnnotationClass = "HEAD".rawClassName("jakarta.ws.rs", false)
    val PatchAnnotationClass = "PATCH".rawClassName("jakarta.ws.rs", false)
    val TraceAnnotationClass = "TRACE".rawClassName("jakarta.ws.rs", false)
    val ObjectMapperClass = "ObjectMapper".rawClassName("com.fasterxml.jackson.databind", false)
    val RestResponseClass = "RestResponse".rawClassName("org.jboss.resteasy.reactive", false)
}

object Library {

    val AllClasses = "*".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi", false)
    val MaybeClass = "Maybe".rawClassName("com.ancientlightstudios.quarkus.kotlin.openapi", false)

}