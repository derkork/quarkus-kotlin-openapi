package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SchemaDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiContentMapping
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelClass

class ModelFeatureTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        spec.inspect {
            bundles {
                requests {
                    parameters { propagateContentType(parameter.content, Direction.Up) }

                    body { propagateContentType(body.content, Direction.Up) }

                    responses {
                        headers { propagateContentType(header.content, Direction.Down) }

                        body { propagateContentType(body.content, Direction.Down) }
                    }
                }
            }
        }
    }

    private fun TransformationContext.propagateContentType(content: OpenApiContentMapping, direction: Direction) {
        val mode = when (serializationDirection) {
            direction -> TransformationMode.Serialization
            else -> TransformationMode.Deserialization
        }

        val schemaDirection = when (direction) {
            Direction.Up -> SchemaDirection.UnidirectionalUp
            Direction.Down -> SchemaDirection.UnidirectionalDown
        }

        // the required flag is not important here, because we are only interested in the final model class
        val modelInstance = modelInstanceFor(content.schema, schemaDirection)
        val modelClass = modelInstance.unwrapModelClass() ?: return

        getHandler<ModelTransformationHandler, Unit> {
            registerTransformations(modelClass, mode, content.mappedContentType)
        }
    }

}

/**
 * handler of this type can install features for a model based on the given content type to e.g. add
 * serialization or deserialization methods to the model implementation
 */
interface ModelTransformationHandler : Handler {

    fun registerTransformations(
        model: ModelClass, mode: TransformationMode, contentType: ContentType
    ): HandlerResult<Unit>

}

enum class TransformationMode {
    Serialization,
    Deserialization
}