package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.SafeObjectBuilderTransformStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.addTransformStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
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
            registerImport("io.quarkus.runtime.annotations.RegisterForReflection")
            registerImport("com.fasterxml.jackson.annotation.JsonProperty")
            registerImport(apiPackage(), wildcardImport = true)

            kotlinClass(fileName, asDataClass = true) {
                addReflectionAnnotation()

                definition.properties.forEach { (name, _, propertyTypeUsage) ->
                    kotlinMember(
                        name.variableName(),
                        propertyTypeUsage.unsafeType,
                        private = false
                    ) {
                        kotlinAnnotation(
                            "field:JsonProperty".rawClassName(),
                            "value".variableName() to name.stringExpression()
                        )
                    }
                }

                kotlinMethod("asSafe".methodName(), returnType = "Maybe".typeName().of(definition.name)) {
                    kotlinParameter("context".variableName(), "String".rawTypeName())

                    val returnStatement = SafeObjectBuilderTransformStatement(definition.name)

                    definition.properties.forEach { (name, _, propertyTypeUsage) ->
                        addTransformStatement(
                            name.variableName(), propertyTypeUsage,
                            "\${context}.$name".stringExpression(), false
                        ).also(returnStatement::addParameter)
                    }

                    addStatement(returnStatement)
                }
            }
        }.also { generateFile(it) }
    }
}