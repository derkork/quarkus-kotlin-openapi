package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.SafeObjectBuilderDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.getDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.FlowDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

class UnsafeObjectModelEmitter(private val direction: FlowDirection) : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions(direction)
            .filterIsInstance<ObjectTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: ObjectTypeDefinition) {
        kotlinFile(modelPackage(), definition.name.extend(postfix = "Unsafe")) {
            registerImport("com.fasterxml.jackson.databind.JsonNode")
            registerImport(apiPackage(), wildcardImport = true)
            validatorPackage?.let {
                registerImport(it, wildcardImport = true)
            }


            kotlinClass(fileName, asDataClass = true) {
                kotlinMember("node".variableName(), "JsonNode".rawTypeName())

                kotlinMethod("asSafe".methodName(), returnType = "Maybe".typeName().of(definition.name)) {
                    kotlinParameter("context".variableName(), "String".rawTypeName())

                    val returnStatement = SafeObjectBuilderDeserializationStatement(definition.name)

                    definition.properties.forEach { (name, _, propertyTypeUsage) ->
                        val source = "node".variableName().propertyToMaybeExpression(name)
                        val parameter = name.variableName().extend(postfix = "maybe")
                        addStatement(getDeserializationStatement(source, parameter, propertyTypeUsage, true))
                        returnStatement.addParameter(parameter)
                    }

                    addStatement(returnStatement)
                }
            }
        }.also { generateFile(it) }
    }
}