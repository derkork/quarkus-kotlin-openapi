package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.toKotlinIdentifier
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ClassName private constructor(private val name: String) : Name {

    fun extend(prefix: String = "", postfix: String = "") = "${prefix}_${name}_$postfix".className()

    override fun render() = name

    companion object {

        fun String.rawClassName() = ClassName(this)

        fun String.className() = ClassName(toKotlinIdentifier()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() })

        fun Name.className() = render().className()

    }

}