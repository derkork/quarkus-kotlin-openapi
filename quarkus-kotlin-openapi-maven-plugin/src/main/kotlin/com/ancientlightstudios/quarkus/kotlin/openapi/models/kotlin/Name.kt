package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter
import java.util.*

class ClassName private constructor(val name: String) {

    override fun toString() = "ClassName($name)"

    companion object {

        fun String.rawClassName() = ClassName(this)

        fun String.className() = ClassName(this.toKotlinIdentifier()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() })
    }

}

sealed class TypeName {

    abstract fun render(writer: CodeWriter)

    class SimpleTypeName private constructor(val name: String, val nullable: Boolean) : TypeName() {

        override fun toString() = "SimpleTypeName($name)"

        override fun render(writer: CodeWriter) = with(writer) {
            write(name)
            if (nullable) {
                write("?")
            }
        }

        companion object {

            fun String.rawTypeName(nullable: Boolean = false) = SimpleTypeName(this, nullable)

            fun String.typeName(nullable: Boolean = false) = SimpleTypeName(
                this.toKotlinIdentifier()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() },
                nullable
            )

            fun ClassName.typeName(nullable: Boolean = false) = name.rawTypeName(nullable)
        }

    }

    class GenericTypeName private constructor(val outerType: SimpleTypeName, val innerType: TypeName) : TypeName() {

        override fun toString() = "GenericTypeName($outerType, $innerType)"

        override fun render(writer: CodeWriter) = with(writer) {
            write("${outerType.name}<")
            innerType.render(this)
            write(">")
            if (outerType.nullable) {
                write("?")
            }
        }

        companion object {

            fun SimpleTypeName.of(inner: TypeName) = GenericTypeName(this, inner)

        }

    }
}

interface Expression {
    val expression: String
}

class StringExpression(private val value: String) : Expression {
    override val expression: String
        get() = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

    companion object {
        fun String.stringExpression() = StringExpression(this)
    }
}

class NestedPathExpression private constructor(private val propertyPath: Expression, private val append: VariableName) :
    Expression {
    override fun toString() = "NestedPath($expression)"
    override val expression: String
        get() = "${propertyPath.expression}.${append.name}"

    companion object {
        fun Expression.nested(name: VariableName) = NestedPathExpression(this, name)
    }
}

class VariableName private constructor(val name: String) : Expression {
    override fun toString() = "VariableName($name)"

    override val expression: String
        get() = name

    companion object {
        fun String.variableName() = VariableName(this.toKotlinIdentifier())
    }
}

class MethodName private constructor(val name: String) {

    override fun toString() = "VariableName($name)"

    companion object {
        fun String.methodName() = MethodName(this.toKotlinIdentifier())
    }

}

private val camelCasePattern = Regex("([a-z])([A-Z])")
private val numberFollowedByLetterPattern = Regex("([0-9])([a-zA-Z])")
private val unwantedGroupPattern = Regex("[^a-zA-Z0-9]+")
private val snakeCasePattern = Regex("_([a-z])")

private fun String.toKotlinIdentifier(): String {
    if (isEmpty()) {
        return ""
    }
    
    // add an underscore into every combination of a lowercase letter followed by an uppercase letter
    val cleaned = this.replace(camelCasePattern, "$1_$2")
        // add an underscore into every combination of a number followed by a letter
        .replace(numberFollowedByLetterPattern, "$1_$2")
        // replace any sequence that is not a letter or a number with a single underscore
        .replace(unwantedGroupPattern, "_")
        // trim off any underscore at the start or end
        .trim('_')
        .lowercase()
        // replace any letter directly behind an underscore with its uppercase buddy
        .replace(snakeCasePattern) { it.groupValues[1].first().titlecase(Locale.ENGLISH) }

    // if it does not start with a letter, prepend an underscore
    return if (cleaned[0].isLetter()) cleaned else "_$cleaned"
}