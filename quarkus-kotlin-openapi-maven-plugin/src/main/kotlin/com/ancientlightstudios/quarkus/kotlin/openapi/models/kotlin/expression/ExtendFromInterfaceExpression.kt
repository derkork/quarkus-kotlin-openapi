package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

class ExtendFromInterfaceExpression(private val name: ClassName) : ExtendExpression {

    override fun evaluate() = name.render()
    
}