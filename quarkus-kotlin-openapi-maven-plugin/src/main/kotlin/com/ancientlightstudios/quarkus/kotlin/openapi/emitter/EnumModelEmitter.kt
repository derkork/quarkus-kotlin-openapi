package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ConstantName.Companion.constantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import com.fasterxml.jackson.databind.JsonNode

class EnumModelEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions()
            .filterIsInstance<EnumTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: EnumTypeDefinition) {
        kotlinFile(modelPackage(), definition.name) {
            registerImport("com.fasterxml.jackson.annotation.JsonProperty")
            registerImport(apiPackage(), wildcardImport = true)
            registerImport("com.fasterxml.jackson.databind.JsonNode")

            kotlinEnum(fileName) {
                kotlinMember("value".variableName(), definition.primitiveType.typeName(), accessModifier = null)
                definition.sourceSchema.values.forEach {
                    // TODO: support different types
                    kotlinEnumItem(it.constantName(), it.stringExpression()) {
                        kotlinAnnotation("JsonProperty".rawClassName(), "value".variableName() to it.stringExpression())
                    }
                }
            }

            val extensionMethodName = fileName.render().methodName().extend(prefix = "as")
            kotlinMethod(
                extensionMethodName,
                returnType = "Maybe".rawTypeName().of(fileName, true),
                receiverType = "String".rawTypeName(true)
            ) {
                kotlinParameter("context".variableName(), "String".typeName())

                kotlinStatement {
                    write("if (this == null) {")
                    indent(newLineBefore = true, newLineAfter = true) {
                        write("return asMaybe(context)")
                    }
                    writeln("}")
                    writeln()
                    write("return when (this) {")
                    indent(newLineBefore = true, newLineAfter = true) {
                        definition.sourceSchema.values.forEach {
                            writeln("\"$it\" -> ${fileName.render()}.${it.className().render()}.asMaybe(context)")
                        }
                        write("else -> Maybe.Failure(context, ValidationError(\"Invalid value for ${fileName.render()}: \$this\", context))")
                    }
                    write("}")
                }
            }

            kotlinMethod(
                extensionMethodName,
                returnType = "Maybe".rawTypeName().of(fileName, true),
                receiverType = "Maybe".rawTypeName().of("String".rawClassName(), true),
                bodyAsAssignment = true
            ) {
                kotlinStatement {
                    write("onNotNull { value.as${fileName.render()}(context) }")
                }
            }

            kotlinMethod(
                extensionMethodName,
                returnType = "Maybe".rawTypeName().of(fileName, true),
                receiverType = "JsonNode".rawTypeName(true),
                bodyAsAssignment = true
            ) {
                kotlinParameter("context".variableName(), "String".typeName())

                kotlinStatement {
                    write("asString(context).as${fileName.render()}()")
                }
            }

            kotlinMethod(
                extensionMethodName,
                returnType = "Maybe".rawTypeName().of(fileName, true),
                receiverType = "Maybe".rawTypeName().of("JsonNode".rawClassName(), true),
                bodyAsAssignment = true
            ) {
                kotlinAnnotation("JvmName".rawClassName(), "name".variableName() to "${extensionMethodName.render()}FromNode".stringExpression())
                kotlinStatement {
                    write("onNotNull { value.as${fileName.render()}(context) }")
                }
            }
        }.also { generateFile(it) }
    }

}