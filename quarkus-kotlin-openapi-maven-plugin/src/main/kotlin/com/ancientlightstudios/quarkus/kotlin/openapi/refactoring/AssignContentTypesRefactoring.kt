package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ContentTypesHint.addContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.Direction
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DirectionHint.addDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DirectionHint.directions
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.pop

// apply flow information (content-types)
class AssignContentTypesRefactoring : SpecRefactoring {

    override fun RefactoringContext.perform() {
        // TODO: not sure where to store this information, as multipart is only important for the first schema and we
        //  don't know what the nested stuff will be yet
//        val tasks = mutableSetOf<TransformableSchemaDefinition>()
//
//        // helper function to avoid code duplication
//        val perform: (TransformableSchemaDefinition, Direction, ContentType) -> Unit =
//            { definition, direction, contentType ->
//                if (definition.addContentType(direction, contentType)) {
//                    tasks.add(definition)
//                }
//            }
//
//        spec.inspect {
//            bundles {
//                requests {
//                    parameters { perform(parameter.schema.schemaDefinition, Direction.Up, ContentType.TextPlain) }
//
//                    body {
//                        val content = body.content
//                        perform(content.schema.schemaDefinition, Direction.Up, content.mappedContentType)
//                    }
//
//                    responses {
//                        headers { perform(header.schema.schemaDefinition, Direction.Down, ContentType.TextPlain) }
//
//                        body {
//                            val content = body.content
//                            perform(content.schema.schemaDefinition, Direction.Down, content.mappedContentType)
//                        }
//                    }
//                }
//            }
//        }
//
//
//        // now propagate the content-type to any sub schema as they are used the same way too
//        while (tasks.isNotEmpty()) {
//            tasks.pop { current ->
//                current.inspect {
//                    val myDirections = schemaDefinition.directions
//                    nestedSchemas {
//                        if (usage.schemaDefinition.addDirection(*myDirections.toTypedArray())) {
//                            tasks.add(usage.schemaDefinition)
//                        }
//                    }
//                }
//            }
//        }
    }

}