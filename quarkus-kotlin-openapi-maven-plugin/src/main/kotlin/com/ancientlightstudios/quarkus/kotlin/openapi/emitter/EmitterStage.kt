package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class EmitterStage(private val config: Config) : GeneratorStage {

    override fun process(spec: TransformableSpec) {
        val context = EmitterContext(spec, config)

        when (config.interfaceType) {
            InterfaceType.CLIENT -> clientEmitters()
            InterfaceType.SERVER -> serverEmitters()
        }.forEach {
            context.runEmitter(it)
        }
    }

    private fun serverEmitters() = listOf(
        ServerDelegateEmitter(),
        ServerRestInterfaceEmitter(config.pathPrefix),
        ServerRequestContainerEmitter(),
        ServerRequestContextEmitter(),
        ModelClassEmitter()
    )

    private fun clientEmitters() = listOf(
        ClientDelegateEmitter(config.interfaceName, config.additionalProviders()),
        ClientRestInterfaceEmitter(),
        ClientResponseContainerEmitter(),
        ModelClassEmitter()
    )

}