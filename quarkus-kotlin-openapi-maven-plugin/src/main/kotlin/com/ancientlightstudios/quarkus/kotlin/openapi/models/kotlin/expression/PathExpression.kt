package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.Name

@Suppress("DataClassPrivateConstructor")
data class PathExpression private constructor(val path: String,val nullable:Boolean = false) : Expression {

    override fun evaluate() = path + if (nullable) "?" else ""

    fun then(segment: Name, nullable: Boolean = false) = PathExpression("${evaluate()}.${segment.render()}", nullable)

    companion object {

        fun Name.pathExpression(nullable: Boolean = false) = PathExpression(this.render(), nullable)

    }

}