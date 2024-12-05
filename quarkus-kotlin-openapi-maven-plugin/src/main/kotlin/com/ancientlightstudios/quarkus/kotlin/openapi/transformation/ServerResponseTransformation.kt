package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContextResponseMethod

class ServerResponseTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        spec.solution.files
            .filterIsInstance<ServerRequestContext>()
            .forEach {
                it.source.inspect {
                    responses {
                        it.methods.add(
                            ServerRequestContextResponseMethod(
                                response.responseCode,
                                response.responseCode,
                                response
                            )
                        )
                    }
                }
            }
    }

}