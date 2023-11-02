package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.ClientResponseContainerEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.ApiSpecTransformer
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.FlowDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.File
import kotlin.io.path.Path

class Generator(private val config: Config) {

    fun generate() = parseApi()
        .transformApi()
        .generateApi()

    private fun parseApi(): ApiSpec {
        val mergedSource = config.sourceFiles
            .map { read(File(it).inputStream()) as ObjectNode }
            .reduce { acc, apiSpec -> acc.merge(apiSpec) }

        val patchedSource = config.patchFiles.map { read(File(it).inputStream()) }
            .fold(mergedSource) { document, patch -> document.patch(patch) }


        if (config.debugOutputFile != null) {
            Path(config.debugOutputFile).parent.toFile().mkdirs()
            File(config.debugOutputFile).writeText(patchedSource.toPrettyString())
        }

        return patchedSource.parseAsApiSpec(RequestFilter(config.endpoints))
    }

    private fun ApiSpec.transformApi() = ApiSpecTransformer(this).transform(config.interfaceName)

    private fun Pair<RequestSuite, TypeDefinitionRegistry>.generateApi() {
        val context = EmitterContext(config.packageName, Path(config.outputDirectory), config.pathPrefix, config.validatorPackageName)

        val codeEmitters = mutableListOf<CodeEmitter>()

        // Server or Both
        if (config.interfaceType != InterfaceType.CLIENT) {
            codeEmitters.add(ServerRestInterfaceEmitter())
            codeEmitters.add(ServerDelegateEmitter())
            codeEmitters.add(ServerRequestContainerEmitter())
            codeEmitters.add(ServerResponseContainerEmitter())
            codeEmitters.add(UnsafeObjectModelEmitter(FlowDirection.Up))
            codeEmitters.add(UnsafeAnyOfModelEmitter(FlowDirection.Up))
            codeEmitters.add(UnsafeOneOfModelEmitter(FlowDirection.Up))
        }

        // Client or Both
        if (config.interfaceType != InterfaceType.SERVER) {
            codeEmitters.add(ClientRestInterfaceEmitter())
            codeEmitters.add(ClientDelegateEmitter())
            codeEmitters.add(ClientResponseContainerEmitter())
            codeEmitters.add(UnsafeObjectModelEmitter(FlowDirection.Down))
            codeEmitters.add(UnsafeAnyOfModelEmitter(FlowDirection.Down))
            codeEmitters.add(UnsafeOneOfModelEmitter(FlowDirection.Down))
        }

        codeEmitters.add(EnumModelEmitter())
        codeEmitters.add(SafeObjectModelEmitter())
        codeEmitters.add(SafeAnyOfModelEmitter())
        codeEmitters.add(SafeOneOfModelEmitter())

        codeEmitters.forEach {
            it.apply { context.emit(first, second) }
        }
    }

}
