package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.RequestInspection
import com.ancientlightstudios.quarkus.kotlin.openapi.inspection.inspect
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.ParameterVariableNameHint.parameterVariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.RequestContainerClassNameHint.requestContainerClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeUsageHint.typeUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

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
                kotlinMember(parameter.parameterVariableName, parameter.schema.buildValidType(), accessModifier = null)
            }

            body {
                content {
                    kotlinMember("body".variableName(), content.schema.buildValidType(), accessModifier = null)
                }
            }
        }
    }

}