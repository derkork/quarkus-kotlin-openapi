package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class IfElseExpression(private val expression: KotlinExpression) : KotlinExpression, IfElseAware, StatementAware {

    private val statements = KotlinStatementContainer()
    private val branches = mutableListOf<IfElseOption>()

    override fun addStatement(statement: KotlinStatement) {
        statements.addItem(statement)
    }

    override fun addElse(branch: IfElseOption) {
        branches.add(branch)
    }

    override fun ImportCollector.registerImports() {
        registerFrom(expression)
        registerFrom(statements)
        registerFrom(branches)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("if (")
        expression.render(this)
        write(") ")
        block(newLineAfter = false) {
            statements.render(this)
        }
        branches.forEach {
            write(" ")
            it.render(this)
        }
    }

    companion object {

        fun ifElseExpression(expression: KotlinExpression, block: IfElseExpression.() -> Unit) =
            IfElseExpression(expression).apply(block)

    }

}