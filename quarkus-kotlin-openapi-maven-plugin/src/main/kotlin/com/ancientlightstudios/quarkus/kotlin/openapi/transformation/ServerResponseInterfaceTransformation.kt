package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

// generates the response interfaces for a server implementation
class ServerResponseInterfaceTransformation : SpecTransformation {

    override fun TransformationContext.perform() {
        if (config.interfaceType != InterfaceType.SERVER) {
            return
        }

        val uniqueNames = mutableSetOf<String>()

        spec.inspect {
            bundles {
                requests {
                    responses {
                        response.interfaceName?.let {
                            // shared responses are duplicated into each request by the open api parser. So it's
                            // not possible to decide here, if this is the same response or another response with
                            // just the same interface name. First case is ok and we can ignore the duplicates.
                            // For the second case we have to relly on the compiler to detect incompatibilities.
                            // Maybe changing the behaviour of the parser would be a good idea, but introduces other
                            // issues in the generator pipeline
                            if (uniqueNames.add(it)) {
                                val responseInterface = ServerResponseInterface(
                                    ComponentName(it, config.packageName, ConflictResolution.Requested),
                                    response.responseCode.asMethodName(),
                                    response.responseCode,
                                    response
                                )

                                headers {
                                    responseInterface.headers += ServerResponseHeader(
                                        variableNameOf(header.name),
                                        header.name,
                                        contentModelFor(header.content, Direction.Down, header.required),
                                        header
                                    )
                                }

                                body {
                                    responseInterface.body = ServerResponseBody(
                                        variableNameOf("body"),
                                        contentModelFor(body.content, Direction.Down, body.required),
                                        body
                                    )
                                }

                                spec.solution.files.add(responseInterface)
                            }
                        }
                    }
                }
            }
        }
    }
}