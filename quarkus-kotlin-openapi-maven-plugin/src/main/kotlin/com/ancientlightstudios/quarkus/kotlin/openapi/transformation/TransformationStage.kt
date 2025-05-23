package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.GeneratorStage
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class TransformationStage(private val config: Config, private val handlerRegistry: HandlerRegistry) : GeneratorStage {

    override fun process(spec: OpenApiSpec) {
        listOf(
            ModelTransformation(),
            ModelFeatureTransformation(),
            DependencyContainerTransformation(),
            ServerTransformation(),
            ClientTransformation(),
            TestClientTransformation(),

            // make sure, all solution files have unique names
            UniqueNameTransformation()
        ).runTransformations(TransformationContext(spec, config, handlerRegistry))
    }

    private fun List<SpecTransformation>.runTransformations(context: TransformationContext) {
        forEach {
            it.apply { context.perform() }
        }
    }

}