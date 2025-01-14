package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.methodNameOf

class JsonDeserializationHandler : DeserializationHandler, EnumModelDeserializationHandler,
    ObjectModelDeserializationHandler, OneOfModelDeserializationHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun deserializationExpression(
        source: KotlinExpression, model: ModelUsage, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationJson) {
        deserializationExpression(source, model)
    }

    private fun deserializationExpression(source: KotlinExpression, model: ModelUsage): KotlinExpression {
        var result = when (val instance = model.instance) {
            is CollectionModelInstance -> collectionDeserialization(source, instance)
            is EnumModelInstance -> enumDeserialization(source, instance)
            is MapModelInstance -> mapDeserialization(source, instance)
            is ObjectModelInstance -> objectDeserialization(source, instance)
            is OneOfModelInstance -> oneOfDeserialization(source, instance)
            is PrimitiveTypeModelInstance -> primitiveDeserialization(source, instance)
        }

        if (!model.isNullable()) {
            result = result.wrap().invoke("required")
        }
        return result
    }

    // produces:
    // <source>.asList()
    //     .mapItems {
    //         <SerializationStatement for nested type>
    //     }
    //     [ValidationStatements]
    private fun collectionDeserialization(source: KotlinExpression, model: CollectionModelInstance): KotlinExpression {
        var result = source.invoke("asList").wrap().invoke("mapItems") {
            deserializationExpression("it".identifier(), model.items).statement()
        }
        result = withValidation(result, model)
        return result
    }

    // produces:
    // <source>.as<ModelName>()
    //     [ValidationStatements]
    private fun enumDeserialization(source: KotlinExpression, model: EnumModelInstance): KotlinExpression {
        val methodName = methodNameOf("as", model.ref.name.name)
        var result = source.invoke(methodName)
        result = withValidation(result, model)
        result = withDefault(result, model)
        return result
    }

    // produces:
    // <source>.asObject()
    //     .propertiesAsMap {
    //         <SerializationStatement for nested type>
    //     }
    //     [ValidationStatements]
    private fun mapDeserialization(source: KotlinExpression, model: MapModelInstance): KotlinExpression {
        var result = source.invoke("asObject").wrap().invoke("propertiesAsMap") {
            deserializationExpression("it".identifier(), model.items).statement()
        }
        result = withValidation(result, model)
        return result
    }

    // produces:
    // <source>.asObject()
    //     .as<ModelName>()
    //     [ValidationStatements]
    private fun objectDeserialization(source: KotlinExpression, model: ObjectModelInstance): KotlinExpression {
        val methodName = methodNameOf("as", model.ref.name.name)
        var result = source.invoke("asObject").wrap().invoke(methodName)
        result = withValidation(result, model)
        return result
    }

    // produces:
    // <source>.asObject()
    //     .as<ModelName>()
    //     [ValidationStatements]
    private fun oneOfDeserialization(source: KotlinExpression, model: OneOfModelInstance): KotlinExpression {
        val methodName = methodNameOf("as", model.ref.name.name)
        var result = source.invoke("asObject").wrap().invoke(methodName)
        result = withValidation(result, model)
        return result
    }

    // produces:
    // <source>.as<BaseType>>()
    //     [ValidationStatements]
    private fun primitiveDeserialization(
        source: KotlinExpression, model: PrimitiveTypeModelInstance
    ): KotlinExpression {
        val methodName = methodNameOf("as", model.itemType.asTypeName().name)
        var result = source.invoke(methodName)
        result = withValidation(result, model)
        result = withDefault(result, model)
        return result
    }

    // produces
    // @JvmName(name = "as<ModelName>FromJson")
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = asString().as<ModelName>()
    override fun KotlinCompanion.installDeserializationFeature(
        model: EnumModelClass, feature: ModelDeserializationFeature
    ) = feature.matches(JsonDeserializationFeature) {
        val methodName = methodNameOf("as", model.name.name)
        kotlinMethod(
            methodName,
            returnType = Library.Maybe.asTypeReference(model.name.asTypeReference().nullable()),
            receiverType = Library.Maybe.asTypeReference(Misc.JsonNode.asTypeReference().nullable()),
            bodyAsAssignment = true
        ) {
            kotlinAnnotation(Kotlin.JvmName, "name" to "${methodName}FromJson".literal())
            invoke("asString").invoke(methodName).statement()
        }
    }

    // produces
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //
    // }
    override fun KotlinCompanion.installDeserializationFeature(
        model: ObjectModelClass, feature: ModelDeserializationFeature
    ) = feature.matches(JsonDeserializationFeature) {
        val methodName = methodNameOf("as", model.name.name)
        kotlinMethod(
            methodName,
            returnType = Library.Maybe.asTypeReference(model.name.asTypeReference().nullable()),
            receiverType = Library.Maybe.asTypeReference(Misc.JsonNode.asTypeReference().nullable()),
            bodyAsAssignment = true
        ) {
            invoke("onNotNull") {
                val objectParts = mutableListOf<InstantiationParameter>()

                if (model.needsPropertiesCount) {
                    val propertyName = "propertyCount"
                    objectParts += PlainParameter(propertyName)

                    val propertiesWithDefault = model.properties.filter { it.model.hasRealDefaultValueDeclared() }
                        .map { it.sourceName.literal() }
                    invoke(
                        "listOf",
                        *propertiesWithDefault.toTypedArray(),
                        genericTypes = listOf(Kotlin.String.asTypeReference())
                    ).declaration("propertiesWithDefault")

                    if (model.additionalProperties != null) {
                        // just count all properties
                        "value".identifier()
                            .invoke("countAllProperties", "propertiesWithDefault".identifier())
                            .declaration(propertyName)
                    } else {
                        // only count known properties
                        val knownProperties = model.properties.map { it.sourceName.literal() }
                        invoke(
                            "listOf",
                            *knownProperties.toTypedArray(),
                            genericTypes = listOf(Kotlin.String.asTypeReference())
                        ).declaration("knownProperties")

                        "value".identifier().invoke(
                            "countKnownProperties",
                            "knownProperties".identifier(),
                            "propertiesWithDefault".identifier()
                        ).declaration(propertyName)
                    }
                }

                // iterate over all members and create a deserialize statement for each
                model.properties.forEach {
                    val statement = "value".identifier().invoke(
                        "findProperty",
                        it.sourceName.literal(),
                        "\${context}.${it.sourceName}".literal()
                    )

                    val expression = registry.getHandler<DeserializationHandler, KotlinExpression> {
                        deserializationExpression(statement, it.model, ContentType.ApplicationJson)
                    }
                    val name = expression.declaration("${it.sourceName}Maybe")
                    objectParts += MaybeParameter(name)
                }

                model.additionalProperties?.let {
                    val protectedNames = model.properties.map { it.sourceName.literal() }
                    val maybe = invoke("propertiesAsMap", *protectedNames.toTypedArray()) {
                        val expression = registry.getHandler<DeserializationHandler, KotlinExpression> {
                            deserializationExpression("it".identifier(), it, ContentType.ApplicationJson)
                        }
                        expression.statement()
                    }
                        .invoke("required").declaration("additionalPropertiesMaybe")

                    objectParts += MaybeParameter(maybe)
                }

                allToObject("context".identifier(), model.name.asTypeName(), objectParts).statement()
            }.statement()
        }
    }

    // produces
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //
    // }
    override fun KotlinCompanion.installDeserializationFeature(
        model: OneOfModelClass, feature: ModelDeserializationFeature
    ) = feature.matches(JsonDeserializationFeature) {

    }

    private fun ModelUsage.hasRealDefaultValueDeclared() = when (instance) {
        is EnumModelInstance -> instance.defaultValue != null
        is PrimitiveTypeModelInstance -> instance.defaultValue != null
        else -> false
    }

}