package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.ParameterKind
import com.ancientlightstudios.quarkus.kotlin.openapi.RequestParameter
import java.io.BufferedWriter

fun RequestParameter.writeUnsafe(context: GenerationContext, writer: BufferedWriter) {
    writeAnnotation(writer)
    writer.write("${name.toKotlinIdentifier()}: String?") // TODO: multi-valued parameters
}

fun RequestParameter.writeSafe(context: GenerationContext, writer: BufferedWriter) {
    writeAnnotation(writer)
    writer.write("${name.toKotlinIdentifier()}: ${type.resolve().toKotlinType(true)}")
}

fun RequestParameter.writeAnnotation(writer: BufferedWriter) {
    when (kind) {
        ParameterKind.PATH -> writer.write("@PathParam(\"$name\") ")
        ParameterKind.QUERY -> writer.write("@QueryParam(\"$name\") ")
        ParameterKind.HEADER -> writer.write("@HeaderParam(\"$name\") ")
        ParameterKind.COOKIE -> writer.write("@CookieParam(\"$name\") ")
    }
}