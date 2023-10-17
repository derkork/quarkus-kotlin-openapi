package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.InvocationExpression.Companion.invocationExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.NullExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.Schema
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.SharedPrimitiveTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.valueExpression

class SharedPrimitiveModelEmitter : CodeEmitter {

    override fun EmitterContext.emit(suite: RequestSuite, typeDefinitionRegistry: TypeDefinitionRegistry) {
        typeDefinitionRegistry.getAllTypeDefinitions()
            .filterIsInstance<SharedPrimitiveTypeDefinition>()
            .forEach {
                emitModel(it)
            }
    }

    private fun EmitterContext.emitModel(definition: SharedPrimitiveTypeDefinition) {
        kotlinFile(modelPackage(), definition.name) {
            registerImport(apiPackage(), wildcardImport = true)

            kotlinValueClass(fileName, definition.primitiveType.typeName()) {
                kotlinAnnotation("JvmInline".className())
            }

            kotlinMethod(
                fileName.render().methodName().extend(prefix = "as"),
                returnType = "Maybe".rawTypeName().of(fileName, true),
                receiverType = "String".rawTypeName(true)
            ) {
                kotlinParameter("context".variableName(), "String".typeName())
                kotlinParameter(
                    "default".variableName(),
                    fileName.typeName(true),
                    getDefault(fileName, definition.primitiveType, definition.sourceSchema)
                )

                kotlinStatement {
                    write("if (this == null) {")
                    indent(newLineBefore = true, newLineAfter = true) {
                        write("return default.asMaybe(context)")
                    }
                    writeln("}")
                    writeln()

                    write("return as${definition.primitiveType.render()}(context)")
                    indent(newLineBefore = true, newLineAfter = true) {
                        write(".mapNotNull { ${fileName.render()}(it) }")
                    }
                }
            }

            kotlinMethod(
                fileName.render().methodName().extend(prefix = "as"),
                returnType = "Maybe".rawTypeName().of(fileName, true),
                receiverType = "Maybe".rawTypeName().of("String".rawClassName(), true),
                bodyAsAssignment = true
            ) {
                kotlinComment {
                    addLine("Ignores any default value and is intended for collections")
                }
                kotlinStatement {
                    write("onNotNull { value.as${fileName.render()}(context, null) }")
                }
            }
        }.also { generateFile(it) }
    }

    private fun getDefault(name: ClassName, primitiveType: ClassName, source: Schema.PrimitiveSchema) =
        when (val value = source.defaultValue) {
            null -> NullExpression
            else -> name.invocationExpression(primitiveType.valueExpression(value))
        }

}