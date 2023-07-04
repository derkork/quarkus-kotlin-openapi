package com.tallence.quarkus.kotlin.openapi

enum class InterfaceType {
    SERVER, CLIENT, BOTH
}

class Config(
    /**
     * The source files to parse.
     */
    val sourceFiles: List<String>,
    /**
     * The name of the interface to generate.
     */
    val interfaceName: String,
    /**
     * The package name of the generated classes.
     */
    val packageName: String,
    /**
     * The directory where the generated sources should be put
     */
    val outputDirectory: String,

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
    val interfaceType: InterfaceType = InterfaceType.BOTH
)