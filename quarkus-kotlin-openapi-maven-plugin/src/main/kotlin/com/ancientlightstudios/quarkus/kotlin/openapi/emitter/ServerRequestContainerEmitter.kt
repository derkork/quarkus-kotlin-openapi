package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.kotlinMember

class ServerRequestContainerEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.inspect {
            bundles {
                requests {
                    if (request.hasInputParameter()) {
                        emitContainerFile().writeFile()
                    }
                }
            }
        }
    }

    private fun RequestInspection.emitContainerFile() = kotlinFile(request.requestContainerClassName) {
        kotlinClass(fileName) {
            parameters {
                val typeUsage = parameter.content.typeUsage
                kotlinMember(
                    parameter.parameterVariableName, typeUsage.buildValidType(),
                    accessModifier = null
                )
            }

            body {
                val typeUsage = body.content.typeUsage
                kotlinMember(
                    body.parameterVariableName, typeUsage.buildValidType(), accessModifier = null
                )
            }
        }
    }

}