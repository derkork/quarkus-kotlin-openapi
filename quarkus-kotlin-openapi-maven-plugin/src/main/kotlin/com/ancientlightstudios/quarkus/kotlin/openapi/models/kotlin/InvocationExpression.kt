package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class InvocationExpression(
    private val receiver: KotlinExpression?, private val method: MethodName,
    private vararg val parameters: Pair<VariableName?, KotlinExpression>,
    private val genericTypes: List<TypeName> = listOf(),
    trailingLambda: StatementAware.() -> Unit = {}
) : KotlinExpression {

    private val trailingLambdaStatements = KotlinStatementContainer()

    init {
        object : StatementAware {
            override fun addStatement(statement: KotlinStatement) {
                trailingLambdaStatements.addItem(statement)
            }
        }.apply(trailingLambda)
    }

    override fun ImportCollector.registerImports() {
        receiver?.let { registerFrom(it) }
        register(method)
        registerFrom(parameters.map { (_, expression) -> expression })
        register(genericTypes)
        registerFrom(trailingLambdaStatements)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        receiver?.let {
            it.render(this)
            write(".")
        }

        write(method.value)
        if (genericTypes.isNotEmpty()) {
            write(genericTypes.joinToString(prefix = "<", postfix = ">") { it.value  })
        }

        if (parameters.isNotEmpty() || trailingLambdaStatements.isEmpty) {
            write("(")
        }

        parameters.forEachWithStats { status, (name, value) ->
            if (name != null) {
                write("${name.value} = ")
            }
            value.render(this)
            if (!status.last) {
                write(", ")
            }
        }
        if (parameters.isNotEmpty() || trailingLambdaStatements.isEmpty) {
            write(")")
        }

        if (trailingLambdaStatements.isNotEmpty) {
            write(" ")
            block {
                trailingLambdaStatements.render(this)
            }
        }
    }

    companion object {

        fun invoke(method: MethodName, genericTypes: List<TypeName> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, method, genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun invoke(method: MethodName, parameters: List<KotlinExpression>, genericTypes: List<TypeName> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, method, *parameters.map { null to it }.toTypedArray(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun invoke(method: MethodName, vararg parameters: KotlinExpression, genericTypes: List<TypeName> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, method, *parameters.map { null to it }.toTypedArray(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun invoke(method: MethodName, vararg parameters: Pair<VariableName, KotlinExpression>, genericTypes: List<TypeName> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, method, *parameters, genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinExpression.invoke(method: MethodName, genericTypes: List<TypeName> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(this, method, genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinExpression.invoke(methodName: MethodName, parameters: List<KotlinExpression>, genericTypes: List<TypeName> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(this, methodName, *parameters.map { null to it }.toTypedArray(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinExpression.invoke(methodName: MethodName, vararg parameters: KotlinExpression, genericTypes: List<TypeName> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(this, methodName, *parameters.map { null to it }.toTypedArray(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinExpression.invoke(methodName: MethodName, vararg parameters: Pair<VariableName, KotlinExpression>, genericTypes: List<TypeName> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(this, methodName, *parameters, genericTypes = genericTypes, trailingLambda = trailingLambda)

    }

}


