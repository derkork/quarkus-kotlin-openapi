package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName

class CombineIntoObjectStatementEmitter(
    private val context: KotlinExpression,
    private val containerClassName: ClassName,
    private val parameterNames: List<VariableName>
) : CodeEmitter {

    var resultStatement: KotlinExpression? = null

    override fun EmitterContext.emit() {
        if (parameterNames.isEmpty()) {
            return
        }

        val parameterExpressions = parameterNames.map {
            it.cast(Library.MaybeSuccessClass.typeName()).property("value".variableName())
        }

        val maybeParameters = listOf(context, *parameterNames.toTypedArray())
        resultStatement = InvocationExpression.invoke("maybeAllOf".rawMethodName(), maybeParameters) {
            InvocationExpression.invoke(containerClassName.constructorName, parameterExpressions).statement()
        }
    }
}
