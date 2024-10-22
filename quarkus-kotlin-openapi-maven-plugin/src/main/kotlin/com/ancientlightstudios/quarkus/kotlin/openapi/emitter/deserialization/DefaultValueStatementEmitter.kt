package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.wrap

class DefaultValueStatementEmitter(
    private val defaultValue: KotlinExpression?,
    baseStatement: KotlinExpression
) : CodeEmitter {

    var resultStatement = baseStatement

    // if a default value is specified, generates an expression like this
    //
    // <baseStatement>
    //     .default { <defaultValue> }
    override fun EmitterContext.emit() {
        if (defaultValue != null) {
            resultStatement = resultStatement.wrap().invoke("default".rawMethodName()) {
                defaultValue.statement()
            }
        }
    }

}