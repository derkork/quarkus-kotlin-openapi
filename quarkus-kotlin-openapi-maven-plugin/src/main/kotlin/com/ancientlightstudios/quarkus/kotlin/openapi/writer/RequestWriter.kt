package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.SchemaRef
import java.io.BufferedWriter

class InputInfo(val name: String, val type: SchemaRef, val resolvedType:Schema)

fun Request.writeServer(context:GenerationContext, writer: BufferedWriter) {
    // request method
    writer.writeln("@${method.name}")
    // path
    writer.writeln("@Path(\"$path\")")

    // now write the function
    writer.write("suspend fun ${operationId.toKotlinIdentifier()}(")

    // parameters, separated by comma, no comma after the last one

    val inputInfo = mutableListOf<InputInfo>()

    for (parameter in parameters) {
        parameter.writeUnsafe(context, writer)
        writer.write(", ")
        inputInfo.add(InputInfo(parameter.name.toKotlinIdentifier(), parameter.type, context.schemaRegistry.resolve(parameter.type)))
    }

    if (bodyType != null) {
        writer.write(" body: String?")
        inputInfo.add(InputInfo("body", bodyType, context.schemaRegistry.resolve(bodyType)))
    }

    writer.write(")")

    if (returnType != null) {
        val type = context.schemaRegistry.resolve(returnType)
        writer.write(": ${type.toKotlinType(true)}")

    }

    writer.writeln(" {")

    if (inputInfo.isEmpty()) {
        writer.writeln("return delegate.${operationId.toKotlinIdentifier()}()")
        writer.writeln("}")
        return
    }

    for (info in inputInfo) {
        when(info.resolvedType) {
            is Schema.PrimitiveTypeSchema -> writer.writeln("val maybe${info.name} =  ${info.name}.as${info.resolvedType.toKotlinType(true)}(\"${info.name}\")")
            is Schema.ObjectTypeSchema -> writer.writeln("val maybe${info.name} =  ${info.name}.asObject(\"${info.name}\", ${info.resolvedType.toKotlinType(true)}::class.java, objectMapper)")
            is Schema.EnumSchema -> writer.writeln("val maybe${info.name} =  ${info.name}.asEnum(\"${info.name}\", ${info.resolvedType.toKotlinType(true)}::class.java, objectMapper)")
            else -> throw IllegalArgumentException("Unsupported type ${info.resolvedType} for parameter ${info.name} in request ${this.operationId}")
        }
    }

    writer.write("val request = maybeOf(")
    for (info in inputInfo) {
        writer.write("maybe${info.name}, ")
    }
    writer.writeln(")  { ")
    writer.write("(")

    for (info in inputInfo) {
        writer.write("valid${info.name}, ")
    }
    writer.writeln(") -> ${operationId.toKotlinClassName()}Request(")

    for(info in inputInfo) {
        writer.writeln("valid${info.name} as ${info.resolvedType.toKotlinType(true)}, ")
    }
    writer.writeln(")}")
    writer.writeln("return delegate.${operationId.toKotlinIdentifier()}(request)")

    writer.writeln("}")
}

fun Request.writeServerDelegate(context:GenerationContext, writer: BufferedWriter) {
    if (parameters.isEmpty() && bodyType == null) {
        writer.write("suspend fun ${operationId.toKotlinIdentifier()}()")
    } else {
        writer.write("suspend fun ${operationId.toKotlinIdentifier()}(request: ${operationId.toKotlinClassName()}Request)")
    }

    if (returnType != null) {
        val type = context.schemaRegistry.resolve(returnType)
        writer.write(": ${type.toKotlinType(true)}")
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
        val type = context.schemaRegistry.resolve(bodyType)
        writer.write(" body: ${type.toKotlinType(true)}")
    }

    writer.writeln(")")

    if (returnType != null) {
        val type = context.schemaRegistry.resolve(returnType)
        writer.writeln(": ${type.toKotlinType(false)}?")
    }
}
