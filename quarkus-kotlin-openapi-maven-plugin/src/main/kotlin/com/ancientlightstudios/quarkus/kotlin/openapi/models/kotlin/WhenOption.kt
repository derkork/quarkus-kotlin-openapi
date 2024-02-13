package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class WhenOption(private val leftSide: KotlinExpression) : KotlinRenderable, StatementAware {

    private val statements = KotlinStatementContainer()

    override fun addStatement(statement: KotlinStatement) {
        statements.addItem(statement)
    }

    override fun ImportCollector.registerImports() {
        registerFrom(leftSide)
        registerFrom(statements)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        leftSide.render(this)
        write(" -> ")
        if (statements.size > 1) {
            block {
                statements.render(this)
            }
        } else {
            statements.render(this)
        }
    }

}

interface WhenOptionAware {

    fun addOption(option: WhenOption)

}

fun WhenOptionAware.optionBlock(leftSide: KotlinExpression, block: WhenOption.() -> Unit) {
    val content = WhenOption(leftSide).apply(block)
    addOption(content)
}
