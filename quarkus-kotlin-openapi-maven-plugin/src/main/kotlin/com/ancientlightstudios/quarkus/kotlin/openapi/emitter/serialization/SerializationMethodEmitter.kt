package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class SerializationMethodEmitter(
    private val typeDefinition: TypeDefinition,
    private val contentType: ContentType
) : CodeEmitter {

    lateinit var generatedMethod: KotlinMethod

    override fun EmitterContext.emit() {
        generatedMethod = when (typeDefinition) {
            is EnumTypeDefinition -> emitForEnumType()
            is ObjectTypeDefinition -> emitForObjectType(typeDefinition)
            else -> ProbableBug("Unsupported type ${typeDefinition.javaClass} for deserialization method")
        }
    }

    private fun emitForEnumType(): KotlinMethod {
        return when (contentType) {
            ContentType.TextPlain -> emitPlainForEnumType()
            ContentType.ApplicationJson -> emitJsonForEnumType()
            else -> ProbableBug("Unsupported content type $contentType for enum serialization method")
        }
    }

    private fun emitPlainForEnumType() = KotlinMethod(
        "asString".rawMethodName(), returnType = Kotlin.StringClass.typeName(), bodyAsAssignment = true
    ).apply {
        "value".variableName().invoke("asString".rawMethodName()).statement()
    }

    private fun emitJsonForEnumType() = KotlinMethod(
        "asJson".rawMethodName(), returnType = Misc.JsonNodeClass.typeName(), bodyAsAssignment = true
    ).apply {
        "value".variableName().invoke("asJson".rawMethodName()).statement()
    }

    private fun EmitterContext.emitForObjectType(typeDefinition: ObjectTypeDefinition) = KotlinMethod(
        "asJson".rawMethodName(), returnType = Misc.JsonNodeClass.typeName(), bodyAsAssignment = true
    ).apply {
        val required = typeDefinition.required

        var expression = invoke("objectNode".rawMethodName())

        typeDefinition.properties.forEach {
            val isPropertyRequired = required.contains(it.sourceName)
            val serialization = runEmitter(
                SerializationStatementEmitter(
                    it.schema.typeDefinition, !isPropertyRequired, it.name, contentType
                )
            ).resultStatement

            expression = expression.wrap().invoke(
                "setProperty".rawMethodName(),
                it.sourceName.literal(),
                serialization,
                isPropertyRequired.literal()
            )
        }

        expression.statement()
    }
}