package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.RequestParameter
import java.io.BufferedWriter

fun RequestParameter.writeUnsafe(context: GenerationContext, writer: BufferedWriter) {
    val type = context.schemaRegistry.resolve(this.type)
    writer.write("${name.toKotlinIdentifier()}: ${type.toKotlinType(false)}?")
}

fun RequestParameter.writeSafe(context: GenerationContext, writer: BufferedWriter) {
    val type = context.schemaRegistry.resolve(this.type)
    writer.write("${name.toKotlinIdentifier()}: ${type.toKotlinType(true)}")
}