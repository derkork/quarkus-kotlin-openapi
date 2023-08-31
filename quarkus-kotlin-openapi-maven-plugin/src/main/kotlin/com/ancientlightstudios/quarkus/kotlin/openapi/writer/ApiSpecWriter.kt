package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.GenerationContext
import com.ancientlightstudios.quarkus.kotlin.openapi.Schema


fun ApiSpec.writeServerInterface(context: GenerationContext) {
    // the interface is named after the config.interfaceName and is put into the config.packageName

    val file = mkFile(context.config.outputDirectory, context.interfacePackage, "${context.interfaceName}Server")
    file.bufferedWriter().use {
        it.write(
            """
        package ${context.interfacePackage}
        ${imports(context)}
        
        @Path("/")
        class ${context.interfaceName}Server(private val delegate: ${context.interfaceName}, private val objectMapper:ObjectMapper) {
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


fun ApiSpec.writeServerRequests(context: GenerationContext) {
    for (request in requests) {
        val requestInfo = request.asRequestInfo()
        if (!requestInfo.hasInput()) {
            continue
        }

        val file = mkFile(context.config.outputDirectory, context.modelPackage, "${request.operationId.toKotlinClassName()}Request")

        file.bufferedWriter().use {
            it.write(
                """
            package ${context.modelPackage}
            
            import com.fasterxml.jackson.annotation.JsonProperty
            import com.fasterxml.jackson.databind.ObjectMapper
            
            data class ${request.operationId.toKotlinClassName()}Request(
            """.trimIndent()
            )

            for (info in requestInfo.inputInfo) {
                it.write("val ${info.name}: ${info.type.resolve().toKotlinType(true)}")
                if (!info.required) {
                    it.write("?")
                }
                it.write(", ")
            }

            it.write(")")
        }

    }
}
fun ApiSpec.writeServerDelegate(context: GenerationContext) {
    // the interface is named after the config.interfaceName and is put into the config.packageName

    val file = mkFile(context.config.outputDirectory, context.interfacePackage, context.interfaceName)
    file.bufferedWriter().use {
        it.write(
            """
        package ${context.interfacePackage}
        ${imports(context)}
        
        interface ${context.interfaceName} {
        """.trimIndent()
        )

        it.writeln()

        for (request in requests) {
            request.writeServerDelegate(it)
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
        ${imports(context)}
        
        interface ${context.interfaceName} {
        """.trimIndent()
        )

        it.writeln()

        for (request in requests) {
            request.writeClient(context, it)
            it.writeln()
        }

        it.write(
            """
        }
        """.trimIndent()
        )
    }
}

private fun imports(context: GenerationContext) = """ import jakarta.ws.rs.GET
        import jakarta.ws.rs.POST
        import jakarta.ws.rs.PUT
        import jakarta.ws.rs.DELETE
        import jakarta.ws.rs.Path
        import jakarta.ws.rs.PathParam
        import jakarta.ws.rs.QueryParam
        import jakarta.ws.rs.HeaderParam
        import jakarta.ws.rs.CookieParam
        import com.fasterxml.jackson.databind.ObjectMapper

        import ${context.modelPackage}.*
        import com.ancientlightstudios.quarkus.kotlin.openapi.*"""


fun ApiSpec.writeUnsafeSchemas(context: GenerationContext) {
    for (schema in schemas.filter { it !is Schema.PrimitiveTypeSchema && it !is Schema.ArraySchema}) {
        val file = mkFile(context.config.outputDirectory, context.modelPackage, schema.toKotlinType(false))
        file.bufferedWriter().use {
            when (schema) {
                is Schema.ObjectTypeSchema -> schema.writeUnsafe(context, it)
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
    for (schema in schemas.filter { it !is Schema.PrimitiveTypeSchema && it !is Schema.ArraySchema}) {
        val file = mkFile(context.config.outputDirectory, context.modelPackage, schema.toKotlinType(true))
        file.bufferedWriter().use {
            when (schema) {
                is Schema.ObjectTypeSchema -> schema.writeSafe(context, it)
                is Schema.EnumSchema -> schema.writeSafe(context, it)
                is Schema.AllOfSchema -> schema.writeSafe(context, it)
                is Schema.AnyOfSchema -> schema.writeSafe(context, it)
                is Schema.OneOfSchema -> schema.writeSafe(context, it)
                else -> throw IllegalArgumentException("Unknown schema type ${schema::class.simpleName}")
            }
        }
    }
}
