package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class TryCatchOption(private val variableName: String, private val className: KotlinTypeName) : KotlinRenderable,
    StatementAware {

    private val statements = KotlinStatementContainer()

    override fun addStatement(statement: KotlinStatement) {
        statements.addItem(statement)
    }

    override fun ImportCollector.registerImports() {
        register(className)
        registerFrom(statements)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("catch ($variableName: ${className.name}) ")
        block(newLineAfter = false) {
            statements.render(this)
        }
    }

}

interface TryCatchAware {

    fun addCatch(catch: TryCatchOption)

}

fun TryCatchAware.catchBlock(
    className: KotlinTypeName, variableName: String = "e", ignoreVariable: Boolean = false,
    block: TryCatchOption.() -> Unit
) {
    val variable = when (ignoreVariable) {
        true -> "_"
        else -> variableName
    }

    val content = TryCatchOption(variable, className).apply(block)
    addCatch(content)
}
