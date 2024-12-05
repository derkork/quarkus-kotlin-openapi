package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class EmitterContext(val spec: OpenApiSpec, val config: Config) {

    private val _kotlinFiles = mutableListOf<KotlinFile>()
    val kotlinFiles: List<KotlinFile>
        get() = _kotlinFiles

    fun getAdditionalImports() = config.additionalImports()

    fun kotlinFile(name: KotlinTypeName, block: KotlinFile.() -> Unit): KotlinFile {
        val result = KotlinFile(name).apply(block)
        _kotlinFiles.add(result)
        return result
    }
}