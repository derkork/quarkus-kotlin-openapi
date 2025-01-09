package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.ContentTypeHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.FeatureHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class EmitterContext(val spec: OpenApiSpec, val config: Config, val handlerRegistry: HandlerRegistry) {

    private val _kotlinFiles = mutableListOf<KotlinFile>()
    val kotlinFiles: List<KotlinFile>
        get() = _kotlinFiles

    fun kotlinFile(name: KotlinTypeName, block: KotlinFile.() -> Unit): KotlinFile {
        val result = KotlinFile(name).apply(block)
        _kotlinFiles.add(result)
        return result
    }

    inline fun <reified T : ContentTypeHandler> getHandler(contentType: ContentType) =
        handlerRegistry.getHandler<T>(contentType)

    inline fun <reified T : FeatureHandler> getHandler(feature: Feature) =
        handlerRegistry.getHandler<T>(feature)

}