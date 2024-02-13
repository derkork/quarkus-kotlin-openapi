package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class TryCatchExpression : KotlinExpression, TryCatchAware, StatementAware {

    private val statements = KotlinStatementContainer()
    private val catches = mutableListOf<TryCatchOption>()

    override fun addStatement(statement: KotlinStatement) {
        statements.addItem(statement)
    }

    override fun addCatch(catch: TryCatchOption) {
        catches.add(catch)
    }

    override fun ImportCollector.registerImports() {
        registerFrom(statements)
        registerFrom(catches)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("try ")
        block(newLineAfter = false) {
            statements.render(this)
        }
        catches.forEach {
            write(" ")
            it.render(this)
        }
    }

    companion object {

        fun tryExpression(block: TryCatchExpression.() -> Unit) =
            TryCatchExpression().apply(block)
    }

}