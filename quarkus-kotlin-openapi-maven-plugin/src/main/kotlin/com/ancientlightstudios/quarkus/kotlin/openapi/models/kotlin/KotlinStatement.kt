package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

interface KotlinStatement : KotlinRenderable

interface StatementAware {

    fun addStatement(statement: KotlinStatement)

    fun KotlinExpression.statement() {
        addStatement(this)
    }

}