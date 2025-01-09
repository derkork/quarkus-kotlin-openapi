package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.ContentTypeHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ComponentName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ConflictResolution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyVogel

// generates the dependency container which is used at several places within the generated code
class DependencyVogelTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        val dependencyVogel = DependencyVogel(
            ComponentName("DependencyVogel", config.packageName, ConflictResolution.Pinned)
        )

        spec.solution.files.add(dependencyVogel)

        // TODO: this should probably be moved at the end of the transformation queue so we have all the models we need in case of models with sub-types e.g. multipart
        spec.inspect {
            bundles {
                requests {
                    parameters {
                        getHandler<DependencyVogelHandler>(parameter.content.mappedContentType).run {
                            installFeatureFor(dependencyVogel)
                        }
                    }

                    body {
                        getHandler<DependencyVogelHandler>(body.content.mappedContentType).run {
                            installFeatureFor(dependencyVogel)
                        }
                    }

                    responses {
                        headers {
                            getHandler<DependencyVogelHandler>(header.content.mappedContentType).run {
                                installFeatureFor(dependencyVogel)
                            }
                        }

                        body {
                            getHandler<DependencyVogelHandler>(body.content.mappedContentType).run {
                                installFeatureFor(dependencyVogel)
                            }
                        }
                    }
                }
            }
        }
    }

}

interface DependencyVogelHandler : ContentTypeHandler {

    fun installFeatureFor(dependencyVogel: DependencyVogel)

}