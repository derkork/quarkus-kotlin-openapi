package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Schema


fun ApiSpec.writeServerInterface(context: GenerationContext) {
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
            request.writeServer(context, it)
            it.writeln()
        }

        it.write(
            """
        }
        """.trimIndent()
        )
    }
}

fun ApiSpec.writeClientInterface(context: GenerationContext) {
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
            request.writeServer(context, it)
            it.writeln()
        }

        it.write(
            """
        }
        """.trimIndent()
        )
    }
}



fun ApiSpec.writeUnsafeSchemas(context: GenerationContext) {
    for (schema in schemas.filter { it !is Schema.BasicTypeSchema }) {
        val file = mkFile(context.config.outputDirectory, context.modelPackage, schema.toKotlinType(false))
        file.bufferedWriter().use {
            when (schema) {
                is Schema.ComplexSchema -> schema.writeUnsafe(context, it)
                is Schema.EnumSchema -> schema.writeUnsafe(context, it)
                is Schema.AllOfSchema -> schema.writeUnsafe(context, it)
                is Schema.AnyOfSchema -> schema.writeUnsafe(context, it)
                is Schema.OneOfSchema -> schema.writeUnsafe(context, it)
                else -> throw IllegalArgumentException("Unknown schema type ${schema::class.simpleName}")
            }
        }
    }
}

fun ApiSpec.writeSafeSchemas(context: GenerationContext) {
    for (schema in schemas.filter { it !is Schema.BasicTypeSchema }) {
        val file = mkFile(context.config.outputDirectory, context.modelPackage, schema.toKotlinType(true))
        file.bufferedWriter().use {
            when (schema) {
                is Schema.ComplexSchema -> schema.writeSafe(context, it)
                is Schema.EnumSchema -> schema.writeSafe(context, it)
                is Schema.AllOfSchema -> schema.writeSafe(context, it)
                is Schema.AnyOfSchema -> schema.writeSafe(context, it)
                is Schema.OneOfSchema -> schema.writeSafe(context, it)
                else -> throw IllegalArgumentException("Unknown schema type ${schema::class.simpleName}")
            }
        }
    }
}
