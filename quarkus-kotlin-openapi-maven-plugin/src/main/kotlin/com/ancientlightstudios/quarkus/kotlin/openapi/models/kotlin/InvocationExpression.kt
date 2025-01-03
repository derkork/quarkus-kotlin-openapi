package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class InvocationExpression(
    private val receiver: KotlinExpression?, private val target: IdentifierExpression,
    private vararg val parameters: Pair<String?, KotlinExpression>,
    private val genericTypes: List<KotlinTypeReference> = listOf(),
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
        registerFrom(target)
        registerFrom(parameters.map { (_, expression) -> expression })
//        register(genericTypes)
        registerFrom(trailingLambdaStatements)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        receiver?.let {
            it.render(this)
            write(".")
        }

        write(target.value)
        if (genericTypes.isNotEmpty()) {
            write(genericTypes.joinToString(prefix = "<", postfix = ">") { it.render() })
        }

        if (parameters.isNotEmpty() || trailingLambdaStatements.isEmpty) {
            write("(")
        }

        parameters.forEachWithStats { status, (name, value) ->
            if (name != null) {
                write("$name = ")
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

        fun invoke(type: KotlinTypeName, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, type.name.identifier(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun invoke(type: KotlinTypeName, vararg parameters: KotlinExpression, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, type.name.identifier(), *parameters.map { null to it }.toTypedArray(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun invoke(type: KotlinTypeName, vararg parameters: Pair<String, KotlinExpression>, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, type.name.identifier(), *parameters, genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun invoke(method: String, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, method.identifier(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun invoke(method: String, vararg parameters: KotlinExpression, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, method.identifier(), *parameters.map { null to it }.toTypedArray(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun invoke(method: String, vararg parameters: Pair<String, KotlinExpression>, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(null, method.identifier(), *parameters, genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinExpression.invoke(method: String, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(this, method.identifier(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinExpression.invoke(method: String, vararg parameters: KotlinExpression, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(this, method.identifier(), *parameters.map { null to it }.toTypedArray(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinExpression.invoke(method: String, vararg parameters: Pair<String, KotlinExpression>, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(this, method.identifier(), *parameters, genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinTypeName.invoke(method: String, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(IdentifierExpression(name, packageName), method.identifier(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinTypeName.invoke(method: String, vararg parameters: KotlinExpression, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(IdentifierExpression(name, packageName), method.identifier(), *parameters.map { null to it }.toTypedArray(), genericTypes = genericTypes, trailingLambda = trailingLambda)

        fun KotlinTypeName.invoke(method: String, vararg parameters: Pair<String, KotlinExpression>, genericTypes: List<KotlinTypeReference> = listOf(), trailingLambda: StatementAware.() -> Unit = {}): KotlinExpression =
            InvocationExpression(IdentifierExpression(name, packageName), method.identifier(), *parameters, genericTypes = genericTypes, trailingLambda = trailingLambda)

    }

}


