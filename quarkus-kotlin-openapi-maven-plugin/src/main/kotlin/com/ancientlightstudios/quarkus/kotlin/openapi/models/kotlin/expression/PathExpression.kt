package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.Name

@Suppress("DataClassPrivateConstructor")
data class PathExpression private constructor(val path: String) : Expression {

    override fun evaluate() = path

    fun then(segment: Name) = PathExpression("$path.${segment.render()}")

    companion object {

        fun Name.pathExpression() = PathExpression(this.render())

    }

}