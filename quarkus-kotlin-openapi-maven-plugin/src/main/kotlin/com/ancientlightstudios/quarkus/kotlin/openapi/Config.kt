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
    val debugOutputFile:String? = null,

    /**
     * The name of the interface to generate.
     */
    val interfaceName: String,
    /**
     * The package name of the generated classes.
     */
    val packageName: String,

    /**
     * The package name where custom validators are located. Defaults to $packageName.model if not set.
     */
    val validatorPackageName: String?,

    /**
     * The directory where the generated sources should be put
     */
    val outputDirectory: String,

    /**
     * Path prefix to be preprended to generated endpoints.
     */
    val pathPrefix:String = "",

    /**
     * The endpoints of the API for which the interface should be generated.
     * If empty, all endpoints are used.
     * Format:
     * - /openapi/subscription/{SearchTerm} -> matches exactly this endpoint (all methods)
     * - /openapi/subscription/{SearchTerm}:get -> matches only the GET method of this endpoint
     * - /openapi/subscription/{SearchTerm}:get,post -> matches only the GET and POST method of this endpoint
     */
    val endpoints: List<String> = emptyList(),

    /**
     * The type of the interface to generate.
     */
    val interfaceType: InterfaceType
)