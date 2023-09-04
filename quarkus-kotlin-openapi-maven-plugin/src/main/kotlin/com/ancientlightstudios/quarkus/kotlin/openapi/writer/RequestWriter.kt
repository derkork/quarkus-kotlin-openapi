package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.Schema
import java.io.BufferedWriter


fun Request.writeServer(context:GenerationContext, writer: BufferedWriter) {
    // request method
    writer.writeln("@${method.name}")
    // path
    writer.writeln("@Path(\"$path\")")

    // now write the function
    writer.write("suspend fun ${operationId.toKotlinIdentifier()}(")

    // parameters, separated by comma, no comma after the last one

    val requestInfo = this.asRequestInfo()

    for (parameter in parameters) {
        parameter.writeUnsafe(context, writer)
        writer.write(", ")
    }

    if (bodyType != null) {
        writer.write(" body: String?")
    }

    writer.write(")")

    if (returnType != null) {
        writer.write(": ${returnType.resolve().toKotlinType(true)}")

    }

    writer.writeln(" {")

    if (!requestInfo.hasInput()) {
        writer.writeln("return delegate.${operationId.toKotlinIdentifier()}()")
        writer.writeln("}")
        return
    }

    for (info in requestInfo.inputInfo) {

        when(val resolvedType = info.type.resolve()) {
            is Schema.PrimitiveTypeSchema -> writer.writeln("val maybe${info.name} =  ${info.name}.as${resolvedType.toKotlinType(false)}(\"${info.contextPath}\")")
            is Schema.ObjectTypeSchema -> writer.writeln("val maybe${info.name} =  ${info.name}.asObject(\"${info.contextPath}\", ${resolvedType.toKotlinType(false)}::class.java, objectMapper)")
            is Schema.ArraySchema -> writer.writeln("val maybe${info.name} =  ${info.name}.asList<${resolvedType.items.resolve().toKotlinType(false)}>(\"${info.contextPath}\", objectMapper)")
            is Schema.EnumSchema -> writer.writeln("val maybe${info.name} =  ${info.name}.asEnum(\"${info.contextPath}\", ${resolvedType.toKotlinType(true)}::class.java, objectMapper)")
            else -> throw IllegalArgumentException("Unsupported type $resolvedType for parameter ${info.contextPath} in request ${this.operationId}")
        }
        if (info.required) {
            writer.writeln(".required()")
        }
    }

    writer.write("val request = maybeOf(\"request\", ")
    for (info in requestInfo.inputInfo) {
        writer.write("maybe${info.name}, ")
    }
    writer.writeln(") { ")
    writer.write("(")

    for (info in requestInfo.inputInfo) {
        writer.write("valid${info.name}, ")
    }
    writer.writeln(") -> ${operationId.toKotlinClassName()}Request(")

    for(info in requestInfo.inputInfo) {
        writer.write("valid${info.name} as ${info.type.resolve().toKotlinType(true)}")
        if (!info.required) {
            writer.write("?")
        }
        writer.writeln(",")
    }
    writer.writeln(")}")
    writer.writeln("return delegate.${operationId.toKotlinIdentifier()}(request)")

    writer.writeln("}")
}

fun Request.writeServerDelegate(writer: BufferedWriter) {
    if (parameters.isEmpty() && bodyType == null) {
        writer.write("suspend fun ${operationId.toKotlinIdentifier()}()")
    } else {
        writer.write("suspend fun ${operationId.toKotlinIdentifier()}(request: Maybe<${operationId.toKotlinClassName()}Request>)")
    }

    if (returnType != null) {
        writer.write(": ${returnType.resolve().toKotlinType(true)}")
    }

    writer.writeln()
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
        val type = bodyType.resolve()
        writer.write(" body: ${type.toKotlinType(true)}")
    }

    writer.writeln(")")

    if (returnType != null) {
        writer.writeln(": ${returnType.resolve().toKotlinType(false)}?")
    }
}
