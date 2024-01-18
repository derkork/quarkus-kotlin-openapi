package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class InvocationExpression(
    private val receiver: KotlinExpression?, private val method: MethodName,
    private vararg val parameters: Pair<VariableName?, KotlinExpression>
) : KotlinExpression {

    override fun ImportCollector.registerImports() {
        receiver?.let { registerFrom(it) }
        registerFrom(parameters.map { (_, expression) -> expression })
    }

    override fun render(writer: CodeWriter) = with(writer) {
        receiver?.let {
            it.render(this)
            write(".")
        }

        write("${method.value}(")
        parameters.forEachWithStats { status, (name, value) ->
            if (name != null) {
                write("${name.value} = ")
            }
            value.render(this)
            if (!status.last) {
                write(", ")
            }
        }
        write(")")
    }

    companion object {

        fun invoke(method: MethodName) =
            InvocationExpression(null, method)

        fun invoke(method: MethodName, vararg parameters: KotlinExpression) =
            InvocationExpression(null, method, *parameters.map { null to it }.toTypedArray())

        fun invoke(method: MethodName, vararg parameters: Pair<VariableName, KotlinExpression>) =
            InvocationExpression(null, method, *parameters)

        fun KotlinExpression.invoke(method: MethodName) =
            InvocationExpression(this, method)

        fun KotlinExpression.invoke(methodName: MethodName, vararg parameters: KotlinExpression) =
            InvocationExpression(this, methodName, *parameters.map { null to it }.toTypedArray())

        fun KotlinExpression.invoke(methodName: MethodName, vararg parameters: Pair<VariableName, KotlinExpression>) =
            InvocationExpression(this, methodName, *parameters)

    }

}


