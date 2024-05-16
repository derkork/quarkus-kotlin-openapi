package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

interface KotlinStatement : KotlinRenderable

interface StatementAware {

    fun addStatement(statement: KotlinStatement)

    fun KotlinExpression.statement() {
        addStatement(this)
    }

    fun KotlinExpression.assignment(
        variableName: VariableName, modifiable: Boolean = false, reassignment: Boolean = false
    ): VariableName {

        addStatement(object : KotlinStatement {

            override fun ImportCollector.registerImports() {
                registerFrom(this@assignment)
            }

            override fun render(writer: CodeWriter) = with(writer) {
                val modifier = if (modifiable) {
                    "var "
                } else {
                    "val "
                }

                if (!reassignment) {
                    write(modifier)
                }

                write("${variableName.value} = ")
                this@assignment.render(this)
            }
        })

        return variableName
    }

    fun KotlinExpression.returnStatement(alias: String? = null) {
        addStatement(object : KotlinStatement {
            override fun ImportCollector.registerImports() {
                registerFrom(this@returnStatement)
            }

            override fun render(writer: CodeWriter) = with(writer) {
                if (alias != null) {
                    write("return@$alias ")
                } else {
                    write("return ")
                }
                this@returnStatement.render(this)
            }
        })
    }

    fun KotlinExpression.throwStatement() {
        addStatement(object : KotlinStatement {
            override fun ImportCollector.registerImports() {
                registerFrom(this@throwStatement)
            }

            override fun render(writer: CodeWriter) = with(writer) {
                write("throw ")
                this@throwStatement.render(this)
            }
        })
    }

}