package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

interface KotlinExpression : KotlinStatement

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

fun UInt.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}U")
    }

}

fun Long.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}L")
    }

}

fun ULong.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}UL")
    }

}

fun Float.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}F")
    }

}

fun Double.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}")
    }

}

fun Boolean.literal() = object : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) = with(writer) {
        write("${this@literal}")
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