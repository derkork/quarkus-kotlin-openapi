package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Request
import java.io.BufferedWriter

fun Request.writeServer(context:GenerationContext, writer: BufferedWriter) {
    // request method
    writer.writeln("@${method.name}")
    // path
    writer.writeln("@Path(\"$path\")")

    // now write the function
    writer.write("suspend fun ${operationId.toKotlinIdentifier()}(")

    // parameters, separated by comma, no comma after the last one

    for ((index, parameter) in parameters.withIndex()) {
        parameter.writeUnsafe(context, writer)
        if (index < parameters.size - 1) {
            writer.write(", ")
        }
    }


    writer.writeln(")")
}

fun Request.writeClient(context:GenerationContext, writer: BufferedWriter) {
    // request method
    writer.writeln("@${method.name}")
    // path
    writer.writeln("@Path(\"$path\")")

    // now write the function
    writer.write("suspend fun ${operationId.toKotlinIdentifier()}(")

    // parameters, separated by comma, no comma after the last one

    for ((index, parameter) in parameters.withIndex()) {
        parameter.writeSafe(context, writer)
        if (index < parameters.size - 1) {
            writer.write(", ")
        }
    }


    writer.writeln(")")
}