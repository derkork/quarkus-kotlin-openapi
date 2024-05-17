package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

interface KotlinStatement : KotlinRenderable

interface StatementAware {

    fun addStatement(statement: KotlinStatement)

    fun KotlinExpression.statement() {
        addStatement(this)
    }

    fun KotlinExpression.declaration(
        variableName: VariableName, typeName: TypeName? = null, modifiable: Boolean = false
    ): VariableName {

        addStatement(object : KotlinStatement {

            override fun ImportCollector.registerImports() {
                registerFrom(this@declaration)
                typeName?.let { register(typeName) }
            }

            override fun render(writer: CodeWriter) = with(writer) {
                val modifier = if (modifiable) {
                    "var "
                } else {
                    "val "
                }

                write(modifier)
                write(variableName.value)
                typeName?.let { write(": ${it.value}") }
                write(" = ")
                this@declaration.render(this)
            }
        })

        return variableName
    }

    fun KotlinExpression.assignment(variableName: VariableName): VariableName {

        addStatement(object : KotlinStatement {

            override fun ImportCollector.registerImports() {
                registerFrom(this@assignment)
            }

            override fun render(writer: CodeWriter) = with(writer) {
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