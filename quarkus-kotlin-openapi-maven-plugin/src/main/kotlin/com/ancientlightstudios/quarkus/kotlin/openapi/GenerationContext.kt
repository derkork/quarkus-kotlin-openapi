package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.parser.SchemaRegistry

class GenerationContext(
    subPackage:String,
    val schemaRegistry: SchemaRegistry,
    val config: Config,
) {
    val modelPackage = "${config.packageName}.model"
    val interfacePackage = "${config.packageName}." + subPackage
    val interfaceName = config.interfaceName
}