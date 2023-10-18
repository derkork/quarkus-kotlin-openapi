package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
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

        val codeEmitter = mutableListOf<CodeEmitter>()

        // Server or Both
        if (config.interfaceType != InterfaceType.CLIENT) {
            codeEmitter.add(ServerRestInterfaceEmitter())
            codeEmitter.add(ServerDelegateEmitter())
            codeEmitter.add(ServerRequestContainerEmitter())
            codeEmitter.add(ServerResponseContainerEmitter())
            codeEmitter.add(UnsafeObjectModelEmitter(FlowDirection.Up))
        }

        // Client or Both
        if (config.interfaceType != InterfaceType.SERVER) {
            codeEmitter.add(UnsafeObjectModelEmitter(FlowDirection.Down))
        }

        codeEmitter.add(EnumModelEmitter())
        codeEmitter.add(SharedPrimitiveModelEmitter())
        codeEmitter.add(SafeObjectModelEmitter())

        codeEmitter.forEach {
            it.apply { context.emit(first, second) }
        }
    }

}
