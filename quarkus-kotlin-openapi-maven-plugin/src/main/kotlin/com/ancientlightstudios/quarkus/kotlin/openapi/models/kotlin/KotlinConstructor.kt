package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinConstructor(
    private val accessModifier: KotlinAccessModifier? = null
) : KotlinRenderable, AnnotationAware, ParameterAware, StatementAware, CommentAware {

    private val annotations = KotlinAnnotationContainer()
    private val parameters = KotlinRenderableWrapContainer<KotlinParameter>()
    private val statements = KotlinStatementContainer()
    private var comment: KotlinComment? = null
    private val primaryConstructorExpressions = KotlinRenderableWrapContainer<KotlinExpression>(5)

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addParameter(parameter: KotlinParameter) {
        parameters.addItem(parameter)
    }

    override fun addStatement(statement: KotlinStatement) {
        statements.addItem(statement)
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }

    fun addPrimaryConstructorParameter(parameter: KotlinExpression) {
        primaryConstructorExpressions.addItem(parameter)
    }

    override fun ImportCollector.registerImports() {
        registerFrom(annotations)
        registerFrom(parameters)
        registerFrom(statements)
        registerFrom(primaryConstructorExpressions)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        annotations.render(this)
        accessModifier?.let { write("${it.value} ") }

        write("constructor(")
        parameters.render(this)
        write(")")

        write(" : this(")
        primaryConstructorExpressions.render(this)
        write(") ")
        if (statements.isNotEmpty) {
            block {
                statements.render(this)
            }
        }
    }

}

interface ConstructorAware {

    fun addConstructor(constructor: KotlinConstructor)

}

fun ConstructorAware.kotlinConstructor(
    accessModifier: KotlinAccessModifier? = null, block: KotlinConstructor.() -> Unit = {}
) = KotlinConstructor(accessModifier).apply(block).also { addConstructor(it) }
