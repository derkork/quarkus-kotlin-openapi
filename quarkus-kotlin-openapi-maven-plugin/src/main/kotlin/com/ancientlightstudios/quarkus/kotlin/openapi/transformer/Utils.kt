package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinAnnotationContainer
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

fun KotlinAnnotationContainer.addPath(path: String) = add("Path".className(), "value".variableName() to path)
fun KotlinAnnotationContainer.addParam(type: ParameterKind, name: String) =
    add("${type.name}_Param".className(), "value".variableName() to name)

fun jakartaRestImports() = listOf(
    "jakarta.ws.rs.GET",
    "jakarta.ws.rs.POST",
    "jakarta.ws.rs.PUT",
    "jakarta.ws.rs.DELETE",
    "jakarta.ws.rs.Path",
    "jakarta.ws.rs.PathParam",
    "jakarta.ws.rs.QueryParam",
    "jakarta.ws.rs.HeaderParam",
    "jakarta.ws.rs.CookieParam"
)

fun jacksonImports() = listOf("com.fasterxml.jackson.databind.ObjectMapper")

fun modelImports(config: Config) = listOf("${config.packageName}.model.*")

fun libraryImports() = listOf("com.ancientlightstudios.quarkus.kotlin.openapi.*")

data class LoopStatus(val index: Int, val first: Boolean, val last: Boolean)

inline fun <T> Collection<T>.forEachWithStats(action: (status: LoopStatus, T) -> Unit) {
    val lastIndex = size - 1
    this.forEachIndexed { index, item -> action(LoopStatus(index, index == 0, index == lastIndex), item) }
}

inline fun <T> Array<T>.forEachWithStats(action: (status: LoopStatus, T) -> Unit) {
    val lastIndex = size - 1
    this.forEachIndexed { index, item -> action(LoopStatus(index, index == 0, index == lastIndex), item) }
}

fun <T> CodeWriter.renderParameterBlock(
    parameters: List<T>,
    maxSizeForSingleLine: Int = 1,
    block: CodeWriter.(T) -> Unit
) {
    // block to render the parameters. but will be called later
    val parameterBlock: CodeWriter.(Boolean) -> Unit = { newLine ->
        parameters.forEachWithStats { status, it ->
            block(it)
            if (!status.last) {
                write(", ", newLineAfter = newLine)
            }
        }
    }

    if (parameters.size > maxSizeForSingleLine) {
        indent(newLineBefore = true, newLineAfter = true) { parameterBlock(true) }
    } else {
        parameterBlock(false)
    }
}

fun <T : QueueItem> T.enqueue(queue: (QueueItem) -> Unit): T {
    queue(this)
    return this
}

fun SchemaRef.containerAsList(
    innerType: ClassName,
    innerNullable: Boolean = false,
    outerNullable: Boolean = false
): TypeName {
    val schema = this.resolve()
    if (schema !is Schema.ArraySchema) {
        return innerType.typeName(innerNullable)
    }

    // passing the innerNullable argument twice is important here, because nested Lists are treated as inner types as well
    return "List".rawTypeName(outerNullable).of(schema.items.containerAsList(innerType, innerNullable, innerNullable))
}

fun SchemaRef.containerAsArray(
    innerType: ClassName,
    innerNullable: Boolean = false,
    outerNullable: Boolean = false
): TypeName {
    val schema = this.resolve()
    if (schema !is Schema.ArraySchema) {
        return innerType.typeName(innerNullable)
    }

    // passing the innerNullable argument twice is important here, because nested Lists are treated as inner types as well
    return "Array".rawTypeName(outerNullable).of(schema.items.containerAsArray(innerType, innerNullable, innerNullable))
}

fun String.primitiveTypeClass() =
    when (this) {
        "string" -> "String"
        "password" -> "String"
        "integer" -> "Int"
        "int32" -> "Int"
        "int64" -> "Long"
        "float" -> "Float"
        "number" -> "Double"
        "boolean" -> "Boolean"
        else -> throw IllegalArgumentException("Unknown basic type: $this")
    }.rawClassName()
