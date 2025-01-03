package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class TransformationStage(private val config: Config) : GeneratorStage {

    override fun process(spec: OpenApiSpec) {
        // TODO: get list via ServiceLoader to support plugins
        listOf(
            ModelTransformation(),

            ServerTransformation(),
            ServerResponseInterfaceTransformation(),
            ServerRequestTransformation(),
            ServerResponseTransformation()
        ).runTransformations(TransformationContext(spec, config))
    }

    private fun List<SpecTransformation>.runTransformations(context: TransformationContext) {
        forEach {
            it.apply { context.perform() }
        }
    }

}