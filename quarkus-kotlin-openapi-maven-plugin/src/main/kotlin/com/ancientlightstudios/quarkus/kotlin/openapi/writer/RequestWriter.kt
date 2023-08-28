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

    for (parameter in parameters) {
        parameter.writeUnsafe(context, writer)
        writer.write(", ")
    }

    if (bodyType != null) {
        val type = context.schemaRegistry.resolve(bodyType)
        writer.write(" body: ${type.toKotlinType(false)}?")
    }

    writer.writeln(")")

    if (returnType != null) {
        val type = context.schemaRegistry.resolve(returnType)
        writer.writeln(": ${type.toKotlinType(true)}")
    }
}

fun Request.writeClient(context:GenerationContext, writer: BufferedWriter) {
    // request method
    writer.writeln("@${method.name}")
    // path
    writer.writeln("@Path(\"$path\")")

    // now write the function
    writer.write("suspend fun ${operationId.toKotlinIdentifier()}(")

    // parameters, separated by comma, no comma after the last one

    for (parameter in parameters) {
        parameter.writeSafe(context, writer)
        writer.write(", ")
    }

    if (bodyType != null) {
        val type = context.schemaRegistry.resolve(bodyType)
        writer.write(" body: ${type.toKotlinType(true)}")
    }

    writer.writeln(")")

    if (returnType != null) {
        val type = context.schemaRegistry.resolve(returnType)
        writer.writeln(": ${type.toKotlinType(false)}?")
    }
}