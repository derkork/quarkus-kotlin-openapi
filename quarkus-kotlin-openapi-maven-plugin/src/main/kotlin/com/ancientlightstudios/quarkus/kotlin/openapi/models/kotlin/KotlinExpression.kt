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
    Kotlin.BigDecimalClass -> value.bigDecimalLiteral()
    Kotlin.BigIntegerClass -> value.bigIntegerLiteral()
    else -> ProbableBug("Unknown type ${this.value} for literal")
}

// TODO: probably not the best way to implement this (needs support for || and &&, etc). but for now until we have a better approach
fun KotlinExpression.compareWith(other: KotlinExpression, mode: String) = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        registerFrom(this@compareWith)
        registerFrom(other)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        this@compareWith.render(this)
        write(" $mode ")
        other.render(this)
    }

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

fun functionReference(className: ClassName?, methodName: MethodName) = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        className?.let { register(it) }
        register(methodName)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        className?.let { write(it.value) }
        write("::${methodName.value}")
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

fun String.uintLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@uintLiteral}U")
    }

}

fun String.longLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@longLiteral}L")
    }

}

fun String.ulongLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@ulongLiteral}UL")
    }

}

fun String.floatLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@floatLiteral}F")
    }

}

fun String.doubleLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write(this@doubleLiteral)
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

fun String.bigDecimalLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(Kotlin.BigDecimalClass)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("BigDecimal(\"${this@bigDecimalLiteral}\")")
    }

}

fun String.bigIntegerLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(Kotlin.BigIntegerClass)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write("BigInteger(\"${this@bigIntegerLiteral}\")")
    }

}

fun nullLiteral() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("null")
    }

}

fun emptyLambda() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("{}")
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