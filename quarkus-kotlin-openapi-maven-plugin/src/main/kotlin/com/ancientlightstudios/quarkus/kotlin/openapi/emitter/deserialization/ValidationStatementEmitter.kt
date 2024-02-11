package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition

class ValidationStatementEmitter(
    private val typeDefinition: TypeDefinition,
    baseStatement: KotlinExpression,
) : CodeEmitter {

    var resultStatement = baseStatement

    override fun EmitterContext.emit() {
        // TODO custom validation
        // TODO string validation
        // TODO number validation
        // TODO array validation
    }

}