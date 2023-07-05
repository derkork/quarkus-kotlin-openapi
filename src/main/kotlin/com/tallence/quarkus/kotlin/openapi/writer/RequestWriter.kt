package com.tallence.quarkus.kotlin.openapi.writer

import com.tallence.quarkus.kotlin.openapi.GenerationContext
import com.tallence.quarkus.kotlin.openapi.Request
import java.io.BufferedWriter

fun Request.write(context:GenerationContext, writer: BufferedWriter) {
    // request method
    writer.writeln("@${method.name}")
    // path
    writer.writeln("@Path(\"$path\")")

    // if we have no usable operationId, we use the path as operationId
    val operationId = if (operationId.isNullOrBlank()) path else operationId

    // now write the function
    writer.write("suspend fun ${operationId.toKotlinIdentifier()}(")

    // parameters, separated by comma, no comma after the last one

    for ((index, parameter) in parameters.withIndex()) {
        parameter.write(context, writer)
        if (index < parameters.size - 1) {
            writer.write(", ")
        }
    }


    writer.writeln(")")
}