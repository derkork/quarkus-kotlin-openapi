package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.OriginPathHint.originPath
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSchema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelClass
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class TransformationContext(val spec: OpenApiSpec, val config: Config, val handlerRegistry: HandlerRegistry) {

    val serializationDirection = when (config.interfaceType) {
        InterfaceType.SERVER -> Direction.Down
        else -> Direction.Up
    }

    val deserializationDirection = when (config.interfaceType) {
        InterfaceType.SERVER -> Direction.Up
        else -> Direction.Down
    }

    private val unidirectionalUpModels = mutableMapOf<String, ModelClass>()
    private val unidirectionalDownModels = mutableMapOf<String, ModelClass>()
    private val bidirectionalModels = mutableMapOf<String, ModelClass>()

    fun registerModelClass(modelClass: ModelClass) {
        val origin = modelClass.source.originPath
        when (modelClass.direction) {
            SchemaDirection.UnidirectionalUp -> unidirectionalUpModels[origin] = modelClass
            SchemaDirection.UnidirectionalDown -> unidirectionalDownModels[origin] = modelClass
            SchemaDirection.Bidirectional -> bidirectionalModels[origin] = modelClass
        }
    }

    fun <T : ModelClass> getRegisteredModelClass(schema: OpenApiSchema, usageDirection: SchemaDirection): T {
        val origin = schema.originPath
        val result = when (usageDirection) {
            // if a model is requested for the unidirectional up direction we can use its bidirectional version too if there is no up version
            SchemaDirection.UnidirectionalUp -> unidirectionalUpModels[origin] ?: bidirectionalModels[origin]
            // if a model is requested for the unidirectional down direction we can use its bidirectional version too if there is no down version
            SchemaDirection.UnidirectionalDown -> unidirectionalDownModels[origin] ?: bidirectionalModels[origin]
            // if a model is requested for the bidirectional direction, there is no fallback, because it must exist as a bidirectional model
            // otherwise the dependent model couldn't be a bidirectional model itself
            SchemaDirection.Bidirectional -> bidirectionalModels[origin]
        } ?: ProbableBug("no model class registered for schema $origin")

        @Suppress("UNCHECKED_CAST")
        return (result as? T) ?: ProbableBug("incompatible model class found")
    }

    inline fun <reified H : Handler, R> getHandler(block: H.() -> HandlerResult<R>): R =
        handlerRegistry.getHandler<H, R>(block)

}
