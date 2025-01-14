package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.StaticContextExpression.Companion.staticContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class JsonSerializationHandler : SerializationHandler, EnumModelSerializationHandler, ObjectModelSerializationHandler,
    OneOfModelSerializationHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun serializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationJson) {
        serializationExpression(source, model)
    }

    private fun serializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
        var result = source
        if (model.isNullable()) {
            result = result.nullCheck()
        }

        return when (val instance = model.instance) {
            is CollectionModelInstance -> nestedItemsSerialization(result, instance.items)
            is MapModelInstance -> nestedItemsSerialization(result, instance.items)
            is EnumModelInstance,
            is PrimitiveTypeModelInstance,
            is ObjectModelInstance,
            is OneOfModelInstance -> simpleSerialization(result)
        }
    }

    // produces:
    // <source>.asJson {
    //     <SerializationStatement for nested type> [?: NullNode.instance]
    // }
    private fun nestedItemsSerialization(source: KotlinExpression, itemModel: ModelUsage) =
        source.invoke("asJson") {
            val innerStatement = serializationExpression("it".identifier(), itemModel)
            when (itemModel.isNullable()) {
                true -> innerStatement.nullFallback(Misc.NullNode.staticContext().property("instance"))
                else -> innerStatement
            }.statement()
        }

    // produces:
    // <source>.asJson()
    private fun simpleSerialization(source: KotlinExpression) = source.invoke("asJson")

    // produces
    // fun asJson(): JsonNode = value.asJson()
    override fun KotlinEnum.installSerializationFeature(model: EnumModelClass, feature: ModelSerializationFeature) =
        feature.matches(JsonSerializationFeature) {
            kotlinMethod("asJson", returnType = Misc.JsonNode.asTypeReference(), bodyAsAssignment = true) {
                "value".identifier().invoke("asJson").statement()
            }
        }

    // produces
    // fun asJson(): JsonNode = objectNode()
    //     .setProperty("<propertyName>", <SerializationStatement for property>, (true|false))
    //     ...
    override fun KotlinClass.installSerializationFeature(
        model: ObjectModelClass, feature: ModelSerializationFeature
    ) = feature.matches(JsonSerializationFeature) {
        kotlinMethod("asJson", returnType = Misc.JsonNode.asTypeReference(), bodyAsAssignment = true) {
            var expression = invoke("objectNode")

            model.properties.forEach {
                val propertyExpression = registry.getHandler<SerializationHandler, KotlinExpression> {
                    serializationExpression(it.name.identifier(), it.model, ContentType.ApplicationJson)
                }

                expression = expression.wrap().invoke(
                    "setProperty",
                    it.sourceName.literal(),
                    propertyExpression,
                    // only check for required, not !nullable, because we want to include null in the response
                    // if the type is nullable but required
                    it.model.instance.required.literal()
                )
            }

            model.additionalProperties?.let {
                val protectedNames = model.properties.map { it.sourceName.literal() }

                expression = expression.wrap().invoke(
                    "setAdditionalProperties",
                    "additionalProperties".identifier(),
                    *protectedNames.toTypedArray()
                ) {
                    registry.getHandler<SerializationHandler, KotlinExpression> {
                        serializationExpression("it".identifier(), it, ContentType.ApplicationJson)
                    }
                }
            }

            expression.statement()
        }
    }

    // produces
    // fun asJson(): JsonNode = value?.asJson() ?: NullNode.instance
    // or
    // fun asJson(): JsonNode = value.asJson()
    // or if the oneOf has a discriminator
    // fun asJson(): JsonNode = value.copy(<discriminatorProperty> = "<alias>").asJson()
    override fun KotlinInterface.installSerializationFeature(
        model: OneOfModelClass, feature: ModelSerializationFeature
    ) = feature.matches(JsonSerializationFeature) {
        kotlinMethod("asJson", returnType = Misc.JsonNode.asTypeReference())
    }

    override fun KotlinClass.installSerializationFeature(
        model: OneOfModelOption, discriminatorProperty: String?, feature: ModelSerializationFeature
    ) = feature.matches(JsonSerializationFeature) {
        kotlinMethod(
            "asJson", returnType = Misc.JsonNode.asTypeReference(),
            bodyAsAssignment = true, override = true
        ) {
            var statement: KotlinExpression = "value".identifier()
            if (discriminatorProperty != null) {
                statement = statement.invoke(
                    "copy",
                    discriminatorProperty to model.aliases.first().literal()
                )
            }

            statement = registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression(statement, model.model, ContentType.ApplicationJson)
            }

            if (model.model.isNullable()) {
                statement = statement.nullFallback(Misc.NullNode.staticContext().property("instance"))
            }

            statement.statement()
        }
    }
}