package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

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

                        val headers = mutableListOf<ServerResponseHeader>()
                        headers {
                            headers += ServerResponseHeader(
                                variableNameOf(header.name),
                                header.name,
                                contentModelFor(header.content, Direction.Down, header.required),
                                header
                            )
                        }

                        val body = response.body?.let { body ->
                            ServerResponseBody(
                                "body", contentModelFor(body.content, Direction.Down, body.required), body
                            )
                        }

                        it.methods.add(
                            ServerRequestContextResponseMethod(
                                response.responseCode.asMethodName(),
                                response.responseCode,
                                headers,
                                body,
                                responseInterface,
                                response
                            )
                        )
                    }
                }
            }
    }

}
