package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.ParserStage

class Generator(private val config: Config) {

    fun generate() {
        val spec = TransformableSpec()
        ParserStage(config).process(spec)


    }

//    fun generate() = parseApi()
//        .verifyApi()
//        .transformApi()
//        .generateApi()


//    private fun ApiSpec.verifyApi() =  also { ApiSpecVerifier(it).verify() }
//
//    private fun ApiSpec.transformApi() = ApiSpecTransformer(this, config).transform()
//
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
