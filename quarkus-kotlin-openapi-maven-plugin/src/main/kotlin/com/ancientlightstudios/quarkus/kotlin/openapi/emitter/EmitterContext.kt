package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class EmitterContext(val spec: OpenApiSpec, val config: Config, val handlerRegistry: HandlerRegistry) {

    private val _kotlinFiles = mutableListOf<KotlinFile>()
    val kotlinFiles: List<KotlinFile>
        get() = _kotlinFiles

    val withTestSupport = config.interfaceType == InterfaceType.TEST_CLIENT

    fun kotlinFile(name: KotlinTypeName, block: KotlinFile.() -> Unit): KotlinFile {
        val result = KotlinFile(name).apply(block)
        _kotlinFiles.add(result)
        return result
    }

    inline fun <reified H : Handler, R> getHandler(block: H.() -> HandlerResult<R>): R =
        handlerRegistry.getHandler<H, R>(block)

}