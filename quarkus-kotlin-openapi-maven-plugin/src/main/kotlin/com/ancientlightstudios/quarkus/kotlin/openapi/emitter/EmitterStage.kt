package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import org.slf4j.LoggerFactory

class EmitterStage(private val config: Config) : GeneratorStage {

    private val log = LoggerFactory.getLogger(EmitterStage::class.java)

    override fun process(spec: OpenApiSpec) {

        val context = EmitterContext(spec, config)

        when (config.interfaceType) {
            InterfaceType.CLIENT -> clientEmitters()
            InterfaceType.SERVER -> serverEmitters()
            InterfaceType.TEST_CLIENT -> testClientEmitters()
        }.forEach {
            context.runEmitter(it)
        }

        log.info("Generated ${context.filesWritten + context.filesUpToDate} files (${context.filesWritten} new, ${context.filesUpToDate} up-to-date).")
    }

    private fun serverEmitters() = listOf(
        ServerDelegateEmitter(),
        ServerRestInterfaceEmitter(config.pathPrefix),
        ServerRequestContainerEmitter(),
        ServerRequestContextEmitter(),
        ServerResponseInterfaceEmitter(),
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