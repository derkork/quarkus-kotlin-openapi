package com.tallence.quarkus.kotlin.openapi

import com.tallence.quarkus.kotlin.openapi.builder.SchemaRegistry

class GenerationContext(
    /**
     * Whether all input should be assumed to be invalid.
     */
    val assumeInvalidInput: Boolean = false,
    val schemaRegistry: SchemaRegistry,
    val config: Config,
) {
    val modelPackage = "${config.packageName}.model"
    val interfacePackage = "${config.packageName}." + if (assumeInvalidInput) "server" else "client"
    val interfaceName = config.interfaceName
}