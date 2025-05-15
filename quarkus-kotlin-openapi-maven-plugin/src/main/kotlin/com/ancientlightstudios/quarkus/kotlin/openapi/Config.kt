package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ConfigIssue

enum class InterfaceType {
    SERVER, CLIENT, TEST_CLIENT
}

class Config(
    /**
     * The source files to parse.
     */
    val sourceFiles: List<String>,

    /**
     * The JSON-Patch files to apply to the OpenAPI specification.
     */
    val patchFiles: List<String>,

    /**
     * Path where the debug output should be written.
     */
    val debugOutputFile: String? = null,

    /**
     * The name of the interface to generate.
     */
    val interfaceName: String,
    /**
     * The package name of the generated classes.
     */
    val packageName: String,

    /**
     * Additional package/class imports that should be included. These can contain custom validators or custom type converters.
     * Format:
     * - <fully qualified class name>
     * - com.example:Foo
     * - com.example.bar:*
     */
    private val additionalImports: List<String>,

    /**
     * The directory where the generated sources should be put
     */
    val outputDirectory: String,

    /**
     * Path prefix to be prepended to generated endpoints.
     */
    val pathPrefix: String = "",

    /**
     * The endpoints of the API for which the interface should be generated.
     * If empty, all endpoints are used.
     * Format:
     * - /openapi/subscription/{SearchTerm} -> matches exactly this endpoint (all methods)
     * - /openapi/subscription/{SearchTerm}:get -> matches only the GET method of this endpoint
     * - /openapi/subscription/{SearchTerm}:get,post -> matches only the GET and POST method of this endpoint
     */
    val endpoints: List<String> = listOf(),

    val splitByTags: Boolean = false,

    /**
     * The custom type mappings to use for the generated interface.
     * Format:
     * - <type>:<format>=<fully qualified class name>
     * - string:uuid=java.util:UUID
     */
    private val typeMappings: List<String>,

    /**
     * Custom content type mappings to use for the generated interface. Requests and responses will still use the
     * content type defined in the spec, but the generator knows how to handle these content types.
     * Format:
     * - <contentType>=<mappedContentType>
     * - application/jrd+json=application/json
     */
    private val contentTypeMappings: List<String>,

    /**
     * The type of the interface to generate.
     */
    val interfaceType: InterfaceType,

    /**
     * A list of additional provider classes which should be added as @RegisterProvider annotations to the generated interface.
     */
    private val additionalProviders: List<String> = listOf(),

    /**
     * Whether to overwrite existing generated files.
     */
    val forceOverwriteGeneratedFiles: Boolean = false,

    val operationRequestPostfix: String = "Request",
    val operationResponsePostfix: String = "Response",
    val operationHttpResponsePostfix: String = "HttpResponse",
    val operationErrorPostfix: String = "Error",
    val operationContextPostfix: String = "Context",
    val operationBuilderPostfix: String = "Builder",
    val operationValidatorPostfix: String = "Validator",
    val modelNamePrefix: String = "",
    val modelNamePostfix: String = "",

    val onlyProfile: String = "",
    val exceptProfile: String = ""
) {

    fun additionalImports() = additionalImports.map { it.toTypeName("Illegal value for additional import $it") }

    fun typeNameFor(type: String, format: String): KotlinTypeName? {
        val mapping = typeMappings.firstOrNull { it.startsWith("$type:$format=") }?.substringAfter("=") ?: return null
        return mapping.toTypeName("Illegal value for type mapping $type:$format")
    }

    fun contentTypeFor(contentType: String): String? {
        return contentTypeMappings.firstOrNull { it.startsWith("$contentType=") }?.substringAfter("=")
    }

    fun additionalProviders() =
        additionalProviders.map { it.toTypeName("Illegal value for additional provider $it") }

    private fun String.toTypeName(errorMessage: String): KotlinTypeName {
        val parts = split(':', limit = 2)
        if (parts.size != 2) {
            ConfigIssue(errorMessage)
        }

        // TODO: maybe some other class as this is part of the emitter?
        return KotlinTypeName(parts[1], parts[0])
    }

}