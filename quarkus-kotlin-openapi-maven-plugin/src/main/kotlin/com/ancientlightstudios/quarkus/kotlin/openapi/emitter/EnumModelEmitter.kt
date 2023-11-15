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
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.valueExpression
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
            registerImport(apiPackage(), wildcardImport = true)
            registerImport("com.fasterxml.jackson.databind.JsonNode")
            registerImports(additionalImports)

            kotlinEnum(fileName) {
                kotlinMember(
                    "value".variableName(),
                    "kotlin.${definition.primitiveType.render()}".rawTypeName(),
                    accessModifier = null
                )
                definition.sourceSchema.values.forEach {
                    kotlinEnumItem(it.constantName(), definition.primitiveType.valueExpression(it))
                }

                kotlinMethod(
                    fileName.render().methodName().extend(prefix = "from"),
                    returnType = "JsonNode".rawTypeName(),
                    bodyAsAssignment = true
                ) {
                    kotlinStatement {
                        write("value.from${definition.primitiveType.render()}()")
                    }
                }
            }

            val extensionMethodName = fileName.render().methodName().extend(prefix = "as")

            kotlinMethod(
                extensionMethodName,
                returnType = "Maybe".rawTypeName().of(fileName, true),
                receiverType = "Maybe".rawTypeName().of("String".rawClassName(), true),
                bodyAsAssignment = true
            ) {
                kotlinStatement {
                    write("onNotNull ")
                    block {
                        write("when (value) ")
                        block {
                            definition.sourceSchema.values.forEach {
                                writeln("\"$it\" -> ${fileName.render()}.${it.className().render()}.asMaybe(context)")
                            }
                            write("else -> Maybe.Failure(context, ValidationError(\"Invalid value for ${fileName.render()}: \$value\", context))")
                        }
                    }
                }
            }

            kotlinMethod(
                extensionMethodName,
                returnType = "Maybe".rawTypeName().of(fileName, true),
                receiverType = "Maybe".rawTypeName().of("JsonNode".rawClassName(), true),
                bodyAsAssignment = true
            ) {
                kotlinAnnotation(
                    "JvmName".rawClassName(),
                    "name".variableName() to "${extensionMethodName.render()}FromNode".stringExpression()
                )
                kotlinStatement {
                    write("asString().as${fileName.render()}()")
                }
            }
        }.also { generateFile(it) }
    }

}