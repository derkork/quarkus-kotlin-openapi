package com.tallence.quarkus.kotlin.openapi.writer

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Basic
import com.tallence.quarkus.kotlin.openapi.ApiSpec
import com.tallence.quarkus.kotlin.openapi.GenerationContext
import com.tallence.quarkus.kotlin.openapi.Schema


fun ApiSpec.writeInterface(context: GenerationContext) {
    // the interface is named after the config.interfaceName and is put into the config.packageName

    val file = mkFile(context.config.outputDirectory, context.interfacePackage, context.interfaceName)
    file.bufferedWriter().use {
        //language=kotlin
        it.write(
            """
        package ${context.interfacePackage}
        import jakarta.ws.rs.GET
        import jakarta.ws.rs.POST
        import jakarta.ws.rs.PUT
        import jakarta.ws.rs.DELETE
        import jakarta.ws.rs.Path
        import ${context.modelPackage}.*
        
        interface ${context.interfaceName} {
        """.trimIndent()
        )

        it.writeln()

        for (request in requests) {
            request.write(context, it)

            it.writeln()
        }

        it.write(
            """
        }
        """.trimIndent()
        )
    }
}


fun ApiSpec.writeSchemas(context: GenerationContext) {
    for (schema in schemas.filter { it !is Schema.BasicTypeSchema }) {
        val file = mkFile(context.config.outputDirectory, context.modelPackage, schema.toKotlinType(!context.assumeInvalidInput))
        file.bufferedWriter().use {
            when (schema) {
                is Schema.ComplexSchema -> schema.write(context, it)
                is Schema.EnumSchema -> schema.write(context, it)
                is Schema.AllOfSchema -> schema.write(context, it)
                is Schema.AnyOfSchema -> schema.write(context, it)
                is Schema.OneOfSchema -> schema.write(context, it)
                else -> throw IllegalArgumentException("Unknown schema type ${schema::class.simpleName}")
            }
        }
    }
}

