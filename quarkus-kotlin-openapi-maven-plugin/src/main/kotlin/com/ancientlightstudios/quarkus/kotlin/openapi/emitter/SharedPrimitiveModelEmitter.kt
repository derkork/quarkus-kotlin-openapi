package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendFromClassExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendFromInterfaceExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.RequestSuite
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.rawTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition.SharedPrimitiveTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.TypeDefinitionRegistry

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

            val nestedType = definition.primitiveTypeName.typeName()
            val extends = listOf(ExtendFromInterfaceExpression("TypeWrapper<${nestedType.render()}>".rawClassName()))
            kotlinValueClass(fileName, nestedType, override = true, extends = extends) {
                kotlinAnnotation("JvmInline".className())
            }

            kotlinMethod(
                fileName.render().methodName().extend(prefix = "as"),
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

                    write("return as${definition.primitiveTypeName.render()}(context)")
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
                kotlinStatement {
                    write("onNotNull { value.as${fileName.render()}(context) }")
                }
            }
        }.also { generateFile(it) }
    }

}