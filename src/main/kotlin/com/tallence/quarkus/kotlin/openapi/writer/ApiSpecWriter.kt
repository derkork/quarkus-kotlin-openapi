package com.tallence.quarkus.kotlin.openapi.writer

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
    for (schema in schemas.filterIsInstance<Schema.ComplexSchema>()) {
        val file = mkFile(context.config.outputDirectory, context.modelPackage, schema.toKotlinType(!context.assumeInvalidInput))
        file.bufferedWriter().use {
            schema.write(context, it)
        }
    }
}

