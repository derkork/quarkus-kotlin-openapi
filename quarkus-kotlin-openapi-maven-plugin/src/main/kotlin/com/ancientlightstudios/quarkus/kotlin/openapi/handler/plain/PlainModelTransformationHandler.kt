package com.ancientlightstudios.quarkus.kotlin.openapi.handler.plain

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.companionMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.StaticContextExpression.Companion.staticContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.ModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationMode
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.methodNameOf
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

class PlainModelTransformationHandler : ModelTransformationHandler, DeserializationHandler,
    EnumModelDeserializationHandler, SerializationHandler, EnumModelSerializationHandler {

    override fun registerTransformations(model: ModelClass, mode: TransformationMode, contentType: ContentType) =
        contentType.matches(ContentType.TextPlain) {
            model.features += when (mode) {
                TransformationMode.Serialization -> PlainSerializationFeature
                TransformationMode.Deserialization -> PlainDeserializationFeature
            }
        }

    override fun deserializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.TextPlain) {
        deserializationExpression(source, model)
    }

    private fun deserializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
        var result = when (val instance = model.instance) {
            is CollectionModelInstance -> collectionDeserialization(source, instance)
            is EnumModelInstance -> enumDeserialization(source, instance)
            is MapModelInstance -> ProbableBug("Plain deserialization not supported for maps")
            is ObjectModelInstance -> ProbableBug("Plain deserialization not supported for objects")
            is OneOfModelInstance -> ProbableBug("Plain deserialization not supported for oneOfs")
            is PrimitiveTypeModelInstance -> primitiveDeserialization(source, instance)
        }

        if (!model.isNullable()) {
            result = result.wrap().invoke("required")
        }
        return result
    }

    override fun serializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.TextPlain) {
        serializationExpression(source, model)
    }

    private fun serializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
        var result = source
        if (model.isNullable()) {
            result = result.nullCheck()
        }

        return when (val instance = model.instance) {
            is CollectionModelInstance -> collectionSerialization(result, instance)
            is EnumModelInstance,
            is PrimitiveTypeModelInstance -> primitiveSerialization(result)

            is MapModelInstance -> ProbableBug("Plain serialization not supported for maps")
            is ObjectModelInstance -> ProbableBug("Plain serialization not supported for objects")
            is OneOfModelInstance -> ProbableBug("Plain serialization not supported for oneOfs")
        }
    }

    // produces:
    // <source>.mapItems {
    //     <SerializationStatement for nested type>
    // }
    //     [ValidationStatements]
    private fun collectionDeserialization(source: KotlinExpression, model: CollectionModelInstance): KotlinExpression {
        var result = source.invoke("mapItems") {
            deserializationExpression("it".identifier(), model.items).statement()
        }
        result = withValidation(result, model)
        return result
    }

    // produces:
    // <source>.map {
    //     <SerializationStatement for nested type>
    // }
    private fun collectionSerialization(source: KotlinExpression, model: CollectionModelInstance) =
        source.invoke("map") {
            serializationExpression("it".identifier(), model.items).statement()
        }

    // produces:
    // <source>.as<ModelName>()
    //     [ValidationStatements]
    private fun enumDeserialization(source: KotlinExpression, model: EnumModelInstance): KotlinExpression {
        val methodName = methodNameOf("as", model.ref.name.name)
        var result = source.invoke(model.ref.name.companionMethod(methodName))
        result = withValidation(result, model)
        result = withDefault(result, model)
        return result
    }

    // produces:
    // <source>.as<BaseType>>()
    //     [ValidationStatements]
    private fun primitiveDeserialization(
        source: KotlinExpression, model: PrimitiveTypeModelInstance
    ): KotlinExpression {
        // don't use methodNameOf here as it would try to beautify the name
        val methodName = "as${model.itemType.asTypeName().name}"
        var result = source.invoke(methodName)
        result = withValidation(result, model)
        result = withDefault(result, model)
        return result
    }

    // produces:
    // <source>.asString()
    private fun primitiveSerialization(source: KotlinExpression) = source.invoke("asString")

    // produces
    // fun Maybe<String?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //     when(value) {
    //         <itemValue> -> success(<ModelName>(<itemValue>))
    //         else -> failure(ValidationError("is not a valid value", context, ErrorKind.Invalid))
    //     }
    // }
    override fun MethodAware.installDeserializationFeature(
        model: EnumModelClass, feature: ModelDeserializationFeature
    ) = feature.matches(PlainDeserializationFeature) {
        val methodName = methodNameOf("as", model.name.name)
        kotlinMethod(
            methodName,
            returnType = Library.Maybe.asTypeReference(model.name.asTypeReference().acceptNull()),
            receiverType = Library.Maybe.asTypeReference(Kotlin.String.asTypeReference().acceptNull()),
            bodyAsAssignment = true
        ) {
            invoke("onNotNull") {
                WhenExpression.whenExpression("value".identifier()) {
                    model.items.forEach {
                        // build something like
                        // "first" -> success(SimpleEnum.First)
                        optionBlock(it.value.literal()) {
                            InvocationExpression.invoke(
                                "success", model.name.staticContext().property(it.name)
                            ).statement()
                        }

                    }

                    // build something like
                    // else -> failure(ValidationError("is not a valid value", context, ErrorKind.Invalid))
                    optionBlock("else".identifier()) {
                        val validationError = InvocationExpression.invoke(
                            Library.ValidationError.identifier(),
                            "is not a valid value".literal(),
                            "context".identifier(),
                            Library.ErrorKind.identifier().property("Invalid")
                        )
                        InvocationExpression.invoke("failure", validationError).statement()
                    }
                }.statement()
            }.statement()
        }
    }

    // produces
    // fun asString(): String = value.asString()
    override fun MethodAware.installSerializationFeature(model: EnumModelClass, feature: ModelSerializationFeature) =
        feature.matches(PlainSerializationFeature) {
            kotlinMethod("asString", returnType = Kotlin.String.asTypeReference(), bodyAsAssignment = true) {
                "value".identifier().invoke("asString").statement()
            }
        }

}