package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

// TODO: replace suspend, private and other stuff with a bit flag .e.g. Suspend | Private to avoid 5 more members for internal etc
class KotlinMethod(
    private val name: MethodName,
    private val suspend: Boolean = false,
    private val returnType: TypeName? = null,
    private val receiverType: TypeName? = null,
    private val bodyAsAssignment: Boolean = false,
    private val accessModifier: KotlinAccessModifier? = null,
    private val override: Boolean = false
) : KotlinRenderable, AnnotationAware, ParameterAware, StatementAware, CommentAware {

    private val annotations = KotlinAnnotationContainer()
    private val parameters = KotlinRenderableWrapContainer<KotlinParameter>()
    private val statements = KotlinStatementContainer()
    private var comment: KotlinComment? = null

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

    override fun ImportCollector.registerImports() {
        returnType?.let { register(it) }
        receiverType?.let { register(it) }
        registerFrom(annotations)
        registerFrom(parameters)
        registerFrom(statements)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        annotations.render(this)
        accessModifier?.let { write("${it.value} ") }

        if (override) {
            write("override ")
        }

        if (suspend) {
            write("suspend ")
        }

        write("fun ")
        if (receiverType != null) {
            write("${receiverType.value}.")
        }
        write("${name.value}(")
        parameters.render(this)
        write(")")

        if (returnType != null) {
            write(": ${returnType.value}")
        }

        if (statements.isNotEmpty) {
            if (!bodyAsAssignment) {
                writeln(" {")
            } else {
                write(" = ")
            }
            indent {
                statements.render(this)
            }
            writeln(forceNewLine = false) // in case the item already rendered a line break

            if (!bodyAsAssignment) {
                write("}")
            }
        }
    }

}

interface MethodAware {

    fun addMethod(method: KotlinMethod)

}

fun MethodAware.kotlinMethod(
    name: MethodName,
    suspend: Boolean = false,
    returnType: TypeName? = null,
    receiverType: TypeName? = null,
    bodyAsAssignment: Boolean = false,
    accessModifier: KotlinAccessModifier? = null,
    override: Boolean = false,
    block: KotlinMethod.() -> Unit = {}
) {
    val content =
        KotlinMethod(name, suspend, returnType, receiverType, bodyAsAssignment, accessModifier, override).apply(block)
    addMethod(content)

}
