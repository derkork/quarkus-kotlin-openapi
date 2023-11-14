package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.writeToJsonNode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.AnyOfTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class SafeAnyOfModelEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions()
            .filterIsInstance<AnyOfTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: AnyOfTypeDefinition) {
        kotlinFile(modelPackage(), definition.name) {
            registerImport("com.fasterxml.jackson.databind.JsonNode")
            registerImport(apiPackage(), wildcardImport = true)

            kotlinClass(fileName, asDataClass = true) {

                definition.schemas.forEach { type ->
                    kotlinMember(
                        type.safeType.variableName(),
                        type.safeType,
                        accessModifier = null
                    )
                }

                // toJsonNode
                kotlinMethod("toJsonNode".methodName(), returnType = "JsonNode".rawTypeName(), bodyAsAssignment = true) {
                    kotlinStatement {
                        definition.schemas.forEachWithStats { status, typeDefinitionUsage ->
                            if (status.first) {
                                writeToJsonNode(typeDefinitionUsage.safeType.variableName(), typeDefinitionUsage)
                            }
                            else {
                                indent {
                                    write(".shallowMerge(")
                                    writeToJsonNode(typeDefinitionUsage.safeType.variableName(), typeDefinitionUsage)
                                    write(")")
                                }
                            }
                            if (status.last) {
                                write("!!")
                            }
                            else {
                                writeln()
                            }
                        }
                    }
                }
            }
        }.also { generateFile(it) }
    }
}