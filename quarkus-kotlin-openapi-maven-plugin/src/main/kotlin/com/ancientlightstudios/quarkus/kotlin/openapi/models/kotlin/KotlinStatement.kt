package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import java.util.*

interface KotlinStatement : KotlinRenderable

interface StatementAware {

    fun addStatement(statement: KotlinStatement)

    fun KotlinExpression.statement() {
        addStatement(this)
    }

    fun KotlinExpression.assignment(modifiable: Boolean = false): VariableName {
        val result = UUID.randomUUID().toString().variableName()
        addStatement(object : KotlinStatement {

            override fun ImportCollector.registerImports() {
                registerFrom(this@assignment)
            }

            override fun render(writer: CodeWriter) = with(writer) {
                val modifier = if (modifiable) {
                    "var"
                } else {
                    "val"
                }

                write("$modifier ${result.value} = ")
                this@assignment.render(this)
            }
        })

        return result
    }

    fun KotlinExpression.assignment(name: VariableName) {
        addStatement(object : KotlinStatement {

            override fun ImportCollector.registerImports() {
                registerFrom(this@assignment)
            }

            override fun render(writer: CodeWriter) = with(writer) {
                write("${name.value} = ")
                this@assignment.render(this)
            }
        })
    }

}