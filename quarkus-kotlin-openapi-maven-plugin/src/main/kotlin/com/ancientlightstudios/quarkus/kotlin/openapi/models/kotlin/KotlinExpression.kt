package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

interface KotlinExpression : KotlinStatement

fun ClassName.literalFor(value: String) = when (this) {
    Kotlin.StringClass -> value.literal()
    Kotlin.IntClass -> value.intLiteral()
    Kotlin.UIntClass -> value.uintLiteral()
    Kotlin.LongClass -> value.longLiteral()
    Kotlin.ULongClass -> value.ulongLiteral()
    Kotlin.FloatClass -> value.floatLiteral()
    Kotlin.DoubleClass -> value.doubleLiteral()
    Kotlin.BooleanClass -> value.booleanLiteral()
    else -> ProbableBug("Unknown type ${this.value} for literal")
}

fun KotlinExpression.nullFallback(fallback: KotlinExpression) = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        registerFrom(this@nullFallback)
        registerFrom(fallback)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("(")
        this@nullFallback.render(this)
        write(" ?: ")
        fallback.render(this)
        write(")")
    }

}

fun KotlinExpression.cast(target: TypeName, safe: Boolean = true) = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        registerFrom(this@cast)
        register(target)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("(")
        this@cast.render(this)
        if (safe) {
            write(" as ")
        } else {
            write(" as? ")
        }
        write(target.value)
        write(")")
    }

}

fun KotlinExpression.wrap() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        registerFrom(this@wrap)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        this@wrap.render(this)
        writeln(false)
    }

}

fun ClassName.javaClass() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(this@javaClass)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@javaClass.value}::class.java")
    }

}

fun ClassName.classExpression() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(this@classExpression)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@classExpression.value}::class")
    }

}

fun ClassName.companionObject() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(this@companionObject)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write(this@companionObject.value)
    }

}

fun functionReference(className: ClassName, methodName: MethodName) = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(className)
        register(methodName)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("${className.value}::${methodName.value}")
    }

}

fun KotlinExpression.spread() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        registerFrom(this@spread)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("*")
        this@spread.render(this)
    }

}

fun String.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("\"${this@literal.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
    }

}

fun Int.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}")
    }

}

fun String.intLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write(this@intLiteral)
    }

}

fun UInt.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}U")
    }

}

fun String.uintLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@uintLiteral}U")
    }

}

fun Long.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}L")
    }

}

fun String.longLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@longLiteral}L")
    }

}

fun ULong.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}UL")
    }

}

fun String.ulongLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@ulongLiteral}UL")
    }

}

fun Float.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}F")
    }

}

fun String.floatLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@floatLiteral}F")
    }

}

fun Double.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}")
    }

}

fun String.doubleLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@doubleLiteral}")
    }

}

fun Boolean.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}")
    }

}

fun String.booleanLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write(this@booleanLiteral)
    }

}

fun nullLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("null")
    }

}

fun <T> Collection<T>.arrayLiteral(block: (T) -> KotlinExpression) = object : KotlinExpression {

    private val items = this@arrayLiteral.map(block)

    override fun ImportCollector.registerImports() {
        registerFrom(items)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("[")
        items.forEachWithStats { status, item ->
            item.render(this)
            if (!status.last) {
                write(", ")
            }
        }
        write("]")
    }
}