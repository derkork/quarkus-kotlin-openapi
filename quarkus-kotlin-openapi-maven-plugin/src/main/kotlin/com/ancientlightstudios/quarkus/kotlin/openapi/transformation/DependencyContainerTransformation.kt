package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ComponentName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ConflictResolution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyContainer
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage

// generates the dependency container which is used at several places within the generated code
class DependencyContainerTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        val dependencyContainer = DependencyContainer(
            ComponentName("DependencyContainer", config.packageName, ConflictResolution.Pinned)
        )

        spec.solution.files.add(dependencyContainer)

        // TODO: this should probably be moved at the end of the transformation queue so we have all the models we need in case of models with sub-types e.g. multipart
        spec.inspect {
            bundles {
                requests {
                    parameters {
                        getHandler<DependencyContainerHandler, Unit> {
                            val content = contentModelFor(parameter.content, Direction.Up, parameter.required)
                            registerDependencies(dependencyContainer, content.model, content.contentType)
                        }
                    }

                    body {
                        getHandler<DependencyContainerHandler, Unit> {
                            val content = contentModelFor(body.content, Direction.Up, body.required)
                            registerDependencies(dependencyContainer, content.model, content.contentType)
                        }
                    }

                    responses {
                        headers {
                            getHandler<DependencyContainerHandler, Unit> {
                                val content = contentModelFor(header.content, Direction.Down, header.required)
                                registerDependencies(dependencyContainer, content.model, content.contentType)
                            }
                        }

                        body {
                            getHandler<DependencyContainerHandler, Unit> {
                                val content = contentModelFor(body.content, Direction.Down, body.required)
                                registerDependencies(dependencyContainer, content.model, content.contentType)
                            }
                        }
                    }
                }
            }
        }
    }

}

/**
 * handler of this type can install features for the `DependencyContainer` component to register additional dependencies
 */
interface DependencyContainerHandler : Handler {

    fun registerDependencies(dependencyContainer: DependencyContainer, model: ModelUsage, contentType: ContentType):
            HandlerResult<Unit>

}