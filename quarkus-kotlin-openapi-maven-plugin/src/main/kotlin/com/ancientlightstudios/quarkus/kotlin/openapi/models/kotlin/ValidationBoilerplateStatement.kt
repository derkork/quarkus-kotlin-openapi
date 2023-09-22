package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class ValidationBoilerplateStatement : KotlinStatement {
    val statementList = KotlinStatementList()

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("return onNotNull {")
        indent {
            statementList.render(this)
        }
        writeln("}")
    }
}

fun KotlinStatement.addTo(boilerplateStatement: ValidationBoilerplateStatement) {
    boilerplateStatement.statementList.statements.add(this)
}