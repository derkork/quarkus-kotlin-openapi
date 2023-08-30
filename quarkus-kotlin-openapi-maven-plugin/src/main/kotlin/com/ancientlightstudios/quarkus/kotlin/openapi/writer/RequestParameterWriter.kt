package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.RequestParameter
import java.io.BufferedWriter

fun RequestParameter.writeUnsafe(context: GenerationContext, writer: BufferedWriter) {
    writeAnnotation(context, writer)
    writer.write("${name.toKotlinIdentifier()}: String?") // TODO: multi-valued parameters
}

fun RequestParameter.writeSafe(context: GenerationContext, writer: BufferedWriter) {
    val type = context.schemaRegistry.resolve(this.type)
    writeAnnotation(context, writer)
    writer.write("${name.toKotlinIdentifier()}: ${type.toKotlinType(true)}")
}

fun RequestParameter.writeAnnotation(context: GenerationContext, writer: BufferedWriter) {
    when (kind) {
        ParameterKind.PATH -> writer.write("@PathParam(\"$name\") ")
        ParameterKind.QUERY -> writer.write("@QueryParam(\"$name\") ")
        ParameterKind.HEADER -> writer.write("@HeaderParam(\"$name\") ")
        ParameterKind.COOKIE -> writer.write("@CookieParam(\"$name\") ")
    }
}