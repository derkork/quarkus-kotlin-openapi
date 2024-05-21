package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class WhenOption(private val leftSide: List<KotlinExpression>) : KotlinRenderable, StatementAware {

    private val statements = KotlinStatementContainer()

    override fun addStatement(statement: KotlinStatement) {
        statements.addItem(statement)
    }

    override fun ImportCollector.registerImports() {
        registerFrom(leftSide)
        registerFrom(statements)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        leftSide.forEachWithStats { status, item ->
            item.render(this)
            if (!status.last) {
                write(", ")
            }
        }

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

fun WhenOptionAware.optionBlock(vararg leftSide: KotlinExpression, block: WhenOption.() -> Unit) {
    val content = WhenOption(leftSide.toList()).apply(block)
    addOption(content)
}
