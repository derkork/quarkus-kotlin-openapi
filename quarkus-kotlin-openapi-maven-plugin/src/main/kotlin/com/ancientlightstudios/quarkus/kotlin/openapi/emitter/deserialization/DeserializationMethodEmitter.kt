package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.companionMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.`when`
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.EnumTypeItem
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.ObjectTypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.TypeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class DeserializationMethodEmitter(
    private val typeDefinition: TypeDefinition,
    private val contentType: ContentType
) : CodeEmitter {

    lateinit var generatedMethod: KotlinMethod

    override fun EmitterContext.emit() {
        generatedMethod = when (typeDefinition) {
            is EnumTypeDefinition -> emitForEnumType(typeDefinition)
            is ObjectTypeDefinition -> emitForObjectType(typeDefinition)
            else -> ProbableBug("Unsupported type ${typeDefinition.javaClass} for deserialization method")
        }
    }

    private fun emitForEnumType(typeDefinition: EnumTypeDefinition): KotlinMethod {
        val methodName = typeDefinition.modelName.companionMethod("as ${typeDefinition.modelName.value}")
        return when (contentType) {
            ContentType.TextPlain -> emitPlainForEnumType(typeDefinition, methodName)
            ContentType.ApplicationJson -> emitJsonForEnumType(methodName)
            else -> ProbableBug("Unsupported content type $contentType for enum deserialization method")
        }
    }

    private fun emitPlainForEnumType(typeDefinition: EnumTypeDefinition, methodName: MethodName): KotlinMethod {
        val methodReturnType = Library.MaybeClass.typeName().of(typeDefinition.modelName, true)
        val methodReceiverType = Library.MaybeClass.typeName().of(Kotlin.StringClass, true)

        return KotlinMethod(
            methodName,
            returnType = methodReturnType,
            receiverType = methodReceiverType,
            bodyAsAssignment = true
        ).apply {
            invoke("onNotNull".rawMethodName()) {
                `when`("value".variableName()) {
                    typeDefinition.items.forEach {
                        generateItemOption(typeDefinition.modelName, it)
                    }
                    generateElseOption(typeDefinition.modelName)
                }.statement()
            }.statement()
        }
    }

    // build something like
    // "first" -> Maybe.Success(context, SimpleEnum.First)
    private fun WhenOptionAware.generateItemOption(enumName: ClassName, item: EnumTypeItem) {
        option(item.value) {
            invoke(
                Library.MaybeSuccessClass.constructorName, "context".variableName(),
                enumName.companionObject().property(item.name)
            ).statement()
        }
    }

    // build something like
    // else -> Maybe.Failure(context, ValidationError("is not a valid value", context))
    private fun WhenOptionAware.generateElseOption(enumName: ClassName) {
        option("else".variableName()) {
            val validationError = invoke(
                Library.ValidationErrorClass.constructorName,
                "is not a valid value".literal(),
                "context".variableName()
            )
            invoke(Library.MaybeFailureClass.constructorName, "context".variableName(), validationError).statement()
        }
    }

    private fun emitJsonForEnumType(methodName: MethodName) = KotlinMethod(
        "asJson".rawMethodName(), returnType = Misc.JsonNodeClass.typeName(), bodyAsAssignment = true
    ).apply {
        "value".variableName().invoke("asJson".rawMethodName()).statement()
    }

    private fun emitForObjectType(typeDefinition: ObjectTypeDefinition) = KotlinMethod(
        "asJson".rawMethodName(), returnType = Misc.JsonNodeClass.typeName(), bodyAsAssignment = true
    ).apply {
    }

}