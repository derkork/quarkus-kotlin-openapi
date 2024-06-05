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
            InterfaceType.TEST_CLIENT -> testClientEmitters()
        }.forEach {
            context.runEmitter(it)
        }
    }

    private fun serverEmitters() = listOf(
        ServerDelegateEmitter(),
        ServerRestInterfaceEmitter(config.pathPrefix),
        ServerRequestContainerEmitter(),
        ServerRequestContextEmitter(),
        ModelClassEmitter(false)
    )

    private fun clientEmitters() = listOf(
        ClientDelegateEmitter(config.pathPrefix, config.interfaceName, config.additionalProviders()),
        ClientRestInterfaceEmitter(),
        ClientResponseContainerEmitter(false),
        ModelClassEmitter(false)
    )

    private fun testClientEmitters() = listOf(
        TestClientRestInterfaceEmitter(config.pathPrefix),
        TestClientResponseValidatorEmitter(),
        TestClientRequestBuilderEmitter(),
        ClientResponseContainerEmitter(true),
        ModelClassEmitter(true)
    )
}