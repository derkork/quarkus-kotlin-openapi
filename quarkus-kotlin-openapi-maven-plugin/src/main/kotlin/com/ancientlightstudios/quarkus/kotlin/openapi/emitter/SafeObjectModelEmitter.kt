package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.writeToJsonNode
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

class SafeObjectModelEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions()
            .filterIsInstance<ObjectTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: ObjectTypeDefinition) {
        kotlinFile(modelPackage(), definition.name) {
            registerImport(apiPackage(), wildcardImport = true)
            registerImport("com.fasterxml.jackson.databind.JsonNode")

            kotlinClass(fileName, asDataClass = true) {

                definition.properties.forEach { (name, _, propertyTypeUsage) ->
                    kotlinMember(
                        name.variableName(),
                        propertyTypeUsage.safeType,
                        accessModifier = null,
                        default = propertyTypeUsage.defaultValue
                    )
                }

                // toJsonNode
                kotlinMethod("toJsonNode".methodName(), returnType = "JsonNode".rawTypeName(), bodyAsAssignment = true) {
                    kotlinStatement {
                        writeln("objectNode()")
                        indent {
                            definition.properties.forEach { (name, _, propertyTypeUsage) ->
                                if (omitNullsInSerialization) {
                                    write(".setNonNull(")
                                }
                                else {
                                    write(".setAny(")
                                }
                                write(name.stringExpression().evaluate())
                                write(", ")
                                writeToJsonNode(name.variableName(), propertyTypeUsage)
                                writeln(")")
                            }
                        }
                    }
                }
            }
        }.also { generateFile(it) }
    }
}