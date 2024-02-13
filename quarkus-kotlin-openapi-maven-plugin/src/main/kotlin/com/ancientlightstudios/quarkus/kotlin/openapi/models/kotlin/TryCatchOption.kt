package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.rawVariableName

class TryCatchOption(private val variableName: VariableName, private val className: ClassName) : KotlinRenderable,
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
        write("catch (${variableName.value}: ${className.value}) ")
        block(newLineAfter = false) {
            statements.render(this)
        }
    }

}

interface TryCatchAware {

    fun addCatch(catch: TryCatchOption)

}

fun TryCatchAware.catchBlock(
    className: ClassName, variableName: VariableName = "e".rawVariableName(), ignoreVariable: Boolean = false,
    block: TryCatchOption.() -> Unit
) {
    val variable = when (ignoreVariable) {
        true -> "_".rawVariableName()
        else -> variableName
    }

    val content = TryCatchOption(variable, className).apply(block)
    addCatch(content)
}
