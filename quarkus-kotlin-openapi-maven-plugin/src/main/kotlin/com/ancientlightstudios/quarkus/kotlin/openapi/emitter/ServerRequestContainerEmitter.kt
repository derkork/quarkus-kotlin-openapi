package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
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
                val typeDefinition = parameter.schema.typeDefinition
                kotlinMember(
                    parameter.parameterVariableName, typeDefinition.buildValidType(!parameter.required),
                    accessModifier = null
                )
            }

            body {
                val typeDefinition = body.content.schema.typeDefinition
                kotlinMember(
                    body.parameterVariableName, typeDefinition.buildValidType(!body.required), accessModifier = null
                )
            }
        }
    }

}