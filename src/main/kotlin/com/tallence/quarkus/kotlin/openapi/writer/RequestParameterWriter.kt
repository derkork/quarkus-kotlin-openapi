package com.tallence.quarkus.kotlin.openapi.writer

import com.tallence.quarkus.kotlin.openapi.GenerationContext
import com.tallence.quarkus.kotlin.openapi.RequestParameter
import java.io.BufferedWriter

fun RequestParameter.write(context: GenerationContext, writer: BufferedWriter) {
    val type = context.schemaRegistry.resolve(this.type)
    writer.write("$name: ${type.toKotlinType(context.assumeInvalidInput)}")
    if (context.assumeInvalidInput) {
        writer.write("?")
    }
}