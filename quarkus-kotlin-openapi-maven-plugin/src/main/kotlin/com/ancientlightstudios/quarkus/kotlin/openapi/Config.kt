package com.ancientlightstudios.quarkus.kotlin.openapi

enum class InterfaceType {
    SERVER, CLIENT
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
     */
    val additionalImports: List<String>,

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
    val endpoints: List<String> = emptyList(),

    val splitByTags: Boolean = false,

    /**
     * The custom type mappings to use for the generated interface.
     * Format:
     * - <type>:<format>=<fully qualified class name>
     * - string:uuid=java.util.UUID
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
     * Whether null values should be omitted in serialization.
     */
    val omitNullsInSerialization: Boolean = true,
    /**
     * A list of additional provider classes which should be added as @RegisterProvider annotations to the generated interface.
     */
    val additionalProviders: List<String> = listOf()
) {

    fun typeNameFor(type: String, format: String): String? {
        return typeMappings.firstOrNull { it.startsWith("$type:$format=") }?.substringAfter("=")
    }

    fun contentTypeFor(contentType: String): String? {
        return contentTypeMappings.firstOrNull { it.startsWith("$contentType=") }?.substringAfter("=")
    }

}