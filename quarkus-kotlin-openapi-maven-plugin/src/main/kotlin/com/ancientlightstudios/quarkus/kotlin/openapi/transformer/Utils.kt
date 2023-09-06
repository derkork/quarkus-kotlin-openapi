package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinAnnotationContainer
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind
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