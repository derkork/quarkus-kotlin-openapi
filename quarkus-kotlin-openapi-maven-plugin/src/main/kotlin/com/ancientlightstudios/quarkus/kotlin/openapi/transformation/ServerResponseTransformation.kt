package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerRequestContextResponseMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ServerResponseInterface

class ServerResponseTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        // a list of all response interfaces for a faster lookup
        val knownInterfaces = spec.solution.files
            .filterIsInstance<ServerResponseInterface>()
            .associateBy { it.name.name }

        spec.solution.files
            .filterIsInstance<ServerRequestContext>()
            .forEach {
                it.source.inspect {
                    responses {
                        val responseInterface = response.interfaceName?.let { knownInterfaces[it] }

                        it.methods.add(
                            ServerRequestContextResponseMethod(
                                response.responseCode.asMethodName(),
                                response.responseCode,
                                responseInterface,
                                response
                            )
                        )
                    }
                }
            }
    }

}