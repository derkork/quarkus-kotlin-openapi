package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.toKotlinIdentifier

@Suppress("DataClassPrivateConstructor")
data class VariableName private constructor(private val name: String) : Name {

    fun extend(prefix: String = "", postfix: String = "") = "${prefix}_${name}_$postfix".variableName()

    override fun render() = name

    companion object {

        fun String.variableName() = VariableName(toKotlinIdentifier())

        fun Name.variableName() = render().variableName()
        
    }

}