package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.toKotlinIdentifier
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ConstantName private constructor(private val name: String) : Name {

    fun extend(prefix: String = "", postfix: String = "") = "${prefix}_${name}_$postfix".constantName()

    override fun render() = name

    companion object {

        fun String.rawConstantName() = ConstantName(this)

        fun String.constantName() = ConstantName(toKotlinIdentifier()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() })

        fun Name.constantName() = render().constantName()

    }

}