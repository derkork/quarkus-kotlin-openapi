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
            it.apply { context.emit() }
        }
    }

    private fun serverEmitters() = listOf(
        ServerDelegateEmitter(),
        ServerRestInterfaceEmitter()
    )

    private fun clientEmitters() = listOf<CodeEmitter>()

    //    private fun Pair<RequestSuite, TypeDefinitionRegistry>.generateApi() {
//        val context = EmitterContext(
//            config.packageName,
//            Path(config.outputDirectory),
//            config.pathPrefix,
//            config.additionalImports,
//            config.omitNullsInSerialization,
//            config.additionalProviders
//        )
//
//        val codeEmitters = mutableListOf<CodeEmitter>()
//
//        // Server or Both
//        if (config.interfaceType != InterfaceType.CLIENT) {
//            codeEmitters.add(ServerRestInterfaceEmitter())
//            codeEmitters.add(ServerDelegateEmitter())
//            codeEmitters.add(ServerRequestContainerEmitter())
//            codeEmitters.add(ServerResponseContainerEmitter())
//            codeEmitters.add(UnsafeObjectModelEmitter(FlowDirection.Up))
//            codeEmitters.add(UnsafeAnyOfModelEmitter(FlowDirection.Up))
//            codeEmitters.add(UnsafeOneOfModelEmitter(FlowDirection.Up))
//        }
//
//        // Client or Both
//        if (config.interfaceType != InterfaceType.SERVER) {
//            codeEmitters.add(ClientRestInterfaceEmitter())
//            codeEmitters.add(ClientDelegateEmitter())
//            codeEmitters.add(ClientResponseContainerEmitter())
//            codeEmitters.add(UnsafeObjectModelEmitter(FlowDirection.Down))
//            codeEmitters.add(UnsafeAnyOfModelEmitter(FlowDirection.Down))
//            codeEmitters.add(UnsafeOneOfModelEmitter(FlowDirection.Down))
//        }
//
//        codeEmitters.add(EnumModelEmitter())
//        codeEmitters.add(SafeObjectModelEmitter())
//        codeEmitters.add(SafeAnyOfModelEmitter())
//        codeEmitters.add(SafeOneOfModelEmitter())
//
//        codeEmitters.forEach {
//            it.apply { context.emit(first, second) }
//        }
//    }

}