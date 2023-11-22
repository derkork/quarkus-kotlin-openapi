package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.OneOfBuilderDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements.getDeserializationStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.OneOfTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.FlowDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

class UnsafeOneOfModelEmitter(private val direction: FlowDirection) : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions(direction)
            .filterIsInstance<OneOfTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: OneOfTypeDefinition) {
        kotlinFile(modelPackage(), definition.name.extend(postfix = "Unsafe")) {
            registerImport("com.fasterxml.jackson.databind.JsonNode")
            registerImport(apiPackage(), wildcardImport = true)
            registerImports(additionalImports)

            kotlinClass(fileName, asDataClass = true) {
                kotlinMember("node".variableName(), "JsonNode".rawTypeName())

                kotlinMethod("asSafe".methodName(), returnType = "Maybe".typeName().of(definition.name)) {
                    kotlinParameter("context".variableName(), "String".rawTypeName())
                    if (definition.discriminator != null) {
                        oneOfWithDiscriminator(definition, definition.discriminator)
                    } else {
                        oneOfWithoutDiscriminator(definition)
                    }
                }
            }
        }.also { generateFile(it) }
    }

    private fun KotlinMethod.oneOfWithoutDiscriminator(definition: OneOfTypeDefinition) {
        val returnStatement = OneOfBuilderDeserializationStatement(definition.name)

        definition.schemas.keys.forEach { schema ->
            val parameter = schema.safeType.variableName().extend(postfix = "maybe")
            val source = "node".variableName().parameterToMaybeExpression(
                "context".variableName().pathExpression()
            )
            addStatement(getDeserializationStatement(source, parameter, schema, true))
            returnStatement.addParameter(parameter, schema)
        }

        addStatement(returnStatement)
    }

    private fun KotlinMethod.oneOfWithDiscriminator(definition: OneOfTypeDefinition, discriminatorProperty:String) {
        kotlinStatement {
            writeln("val discriminator = node.get(${discriminatorProperty.stringExpression().evaluate()})?.asText() ?: return Maybe.Failure(context, ValidationError(\"discriminator field '${discriminatorProperty}' is missing\", context))")
            writeln()
            write("return when(discriminator) ")
            block {
                definition.schemas.forEach { (typeDefinition, aliases) ->

                    val joinedAliases = aliases.joinToString { it.stringExpression().evaluate() }

                    write("$joinedAliases -> ")
                    block(newLineAfter = true) {
                        val parameter = typeDefinition.safeType.variableName().extend(postfix = "maybe")
                        val source = "node".variableName().parameterToMaybeExpression(
                            "context".variableName().pathExpression()
                        )

                        getDeserializationStatement(source, parameter, typeDefinition, true).render(this)

                        writeln(parameter.render())
                        indent {
                            writeln(".onSuccess { success(${definition.name.extend(postfix = typeDefinition.safeType.className().render()).render()}(value)) }")
                        }
                    }
                }
                write("else -> Maybe.Failure(context, ValidationError(\"discriminator field '${discriminatorProperty}' has invalid value '\$discriminator'\", context))")
            }
        }
    }
}