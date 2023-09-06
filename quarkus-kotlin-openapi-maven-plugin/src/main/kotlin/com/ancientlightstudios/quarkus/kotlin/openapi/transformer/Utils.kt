package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinAnnotationContainer
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ParameterKind

fun KotlinAnnotationContainer.addPath(path: String) = add("Path".className(), "value".variableName() to path)
fun KotlinAnnotationContainer.addParam(type: ParameterKind, name: String) =
    add("${type.name}_Param".className(), "value".variableName() to name)

fun KotlinFile.addJakartaRestImports() {
    this.addImport("jakarta.ws.rs.GET")
    this.addImport("jakarta.ws.rs.POST")
    this.addImport("jakarta.ws.rs.PUT")
    this.addImport("jakarta.ws.rs.DELETE")
    this.addImport("jakarta.ws.rs.Path")
    this.addImport("jakarta.ws.rs.PathParam")
    this.addImport("jakarta.ws.rs.QueryParam")
    this.addImport("jakarta.ws.rs.HeaderParam")
    this.addImport("jakarta.ws.rs.CookieParam")
}

fun KotlinFile.addJacksonImports() {
    this.addImport("com.fasterxml.jackson.databind.ObjectMapper")
}

fun KotlinFile.addModelImports(config: Config) {
    this.addImport("${config.packageName}.model.*")
}

fun KotlinFile.addLibraryImports() {
    this.addImport("com.ancientlightstudios.quarkus.kotlin.openapi.*")
}

data class LoopStatus(val index: Int, val first: Boolean, val last: Boolean)
inline fun <T> Collection<T>.forEachWithStats(action: (status: LoopStatus, T) -> Unit) {
    val lastIndex = size - 1
    this.forEachIndexed { index, item -> action(LoopStatus(index, index == 0, index == lastIndex), item) }
}
inline fun <T> Array<T>.forEachWithStats(action: (status: LoopStatus, T) -> Unit) {
    val lastIndex = size - 1
    this.forEachIndexed { index, item -> action(LoopStatus(index, index == 0, index == lastIndex), item) }
}
