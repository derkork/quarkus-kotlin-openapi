package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class IfElseOption(private val expression: KotlinExpression? = null) : KotlinRenderable, StatementAware {

    private val statements = KotlinStatementContainer()

    override fun addStatement(statement: KotlinStatement) {
        statements.addItem(statement)
    }

    override fun ImportCollector.registerImports() {
        expression?.let { registerFrom(it) }
        registerFrom(statements)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("else ")
        if (expression != null) {
            write("if (")
            expression.render(this)
            write(") ")
        }
        block(newLineAfter = false) {
            statements.render(this)
        }
    }

}

interface IfElseAware {

    fun addElse(branch: IfElseOption)

}

fun IfElseAware.elseBlock(expression: KotlinExpression? = null, block: IfElseOption.() -> Unit) {
    val content = IfElseOption(expression).apply(block)
    addElse(content)
}
