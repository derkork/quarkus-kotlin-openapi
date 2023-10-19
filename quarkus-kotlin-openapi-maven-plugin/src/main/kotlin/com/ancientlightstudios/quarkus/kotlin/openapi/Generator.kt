package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ClientResponseContainerEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.RequestFilter
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.merge
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.parseAsApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.read
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.ApiSpecTransformer
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.FlowDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import java.io.File
import kotlin.io.path.Path

class Generator(private val config: Config) {

    fun generate() = parseApi()
        .transformApi()
        .generateApi()

    private fun parseApi() = config.sourceFiles
        .map { read(File(it).inputStream()) }
        .reduce { acc, apiSpec -> acc.merge(apiSpec) }
        .parseAsApiSpec(RequestFilter(config.endpoints))

    private fun ApiSpec.transformApi() = ApiSpecTransformer(this).transform(config.interfaceName)

    private fun Pair<RequestSuite, TypeDefinitionRegistry>.generateApi() {
        val context = EmitterContext(config.packageName, Path(config.outputDirectory))

        val codeEmitters = mutableListOf<CodeEmitter>()

        // Server or Both
        if (config.interfaceType != InterfaceType.CLIENT) {
            codeEmitters.add(ServerRestInterfaceEmitter())
            codeEmitters.add(ServerDelegateEmitter())
            codeEmitters.add(ServerRequestContainerEmitter())
            codeEmitters.add(ServerResponseContainerEmitter())
            codeEmitters.add(UnsafeObjectModelEmitter(FlowDirection.Up))
        }

        // Client or Both
        if (config.interfaceType != InterfaceType.SERVER) {
            codeEmitters.add(ClientRestInterfaceEmitter())
            codeEmitters.add(ClientDelegateEmitter())
            codeEmitters.add(ClientResponseContainerEmitter())
            codeEmitters.add(UnsafeObjectModelEmitter(FlowDirection.Down))
        }

        codeEmitters.add(EnumModelEmitter())
        codeEmitters.add(SharedPrimitiveModelEmitter())
        codeEmitters.add(SafeObjectModelEmitter())

        codeEmitters.forEach {
            it.apply { context.emit(first, second) }
        }
    }

}
