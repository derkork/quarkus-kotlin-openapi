package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.*
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.companionMethod
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.NullCheckExpression.Companion.nullCheck
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.PropertyExpression.Companion.property
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.StaticContextExpression.Companion.staticContext
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.WhenExpression.Companion.whenExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.ModelTransformationHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.TransformationMode
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.methodNameOf

class JsonModelTransformationHandler : ModelTransformationHandler, DeserializationHandler,
    EnumModelDeserializationHandler, ObjectModelDeserializationHandler, OneOfModelDeserializationHandler,
    SerializationHandler, EnumModelSerializationHandler, ObjectModelSerializationHandler,
    OneOfModelSerializationHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun registerTransformations(
        model: ModelClass, mode: TransformationMode, contentType: ContentType
    ) = contentType.matches(ContentType.ApplicationJson) {
        val feature = when (mode) {
            TransformationMode.Serialization -> JsonSerializationFeature
            TransformationMode.Deserialization -> JsonDeserializationFeature
        }

        propagateTransformationFeature(model, feature)
    }

    private fun propagateTransformationFeature(model: ModelClass, feature: Feature) {
        if (!model.features.add(feature)) {
            // this feature was already added
            return
        }

        when (model) {
            is EnumModelClass -> {
                // json deserialization of an enum depends on the plain deserialization, so this feature needs to be added to
                if (feature == JsonDeserializationFeature) {
                    registry.getHandler<ModelTransformationHandler, Unit> {
                        registerTransformations(model, TransformationMode.Deserialization, ContentType.TextPlain)
                    }
                }
                return
            }

            is ObjectModelClass -> {
                model.properties.forEach { propagateTransformationFeature(it.model, feature) }
                model.additionalProperties?.let { propagateTransformationFeature(it, feature) }
            }

            is OneOfModelClass -> {
                model.options.forEach { propagateTransformationFeature(it.model, feature) }
            }
        }
    }

    private fun propagateTransformationFeature(model: ModelUsage, feature: Feature) {
        when (val modelInstance = model.instance) {
            is CollectionModelInstance -> propagateTransformationFeature(modelInstance.items, feature)
            is EnumModelInstance -> propagateTransformationFeature(modelInstance.ref, feature)
            is MapModelInstance -> propagateTransformationFeature(modelInstance.items, feature)
            is ObjectModelInstance -> propagateTransformationFeature(modelInstance.ref, feature)
            is OneOfModelInstance -> propagateTransformationFeature(modelInstance.ref, feature)
            is PrimitiveTypeModelInstance -> return
        }
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
        var result = source.invoke("asObject").wrap().invoke(model.ref.name.companionMethod(methodName))
        result = withValidation(result, model)
        return result
    }

    // produces:
    // <source>.asObject()
    //     .as<ModelName>()
    //     [ValidationStatements]
    private fun oneOfDeserialization(source: KotlinExpression, model: OneOfModelInstance): KotlinExpression {
        val methodName = methodNameOf("as", model.ref.name.name)
        var result = source.invoke("asObject").wrap().invoke(model.ref.name.companionMethod(methodName))
        result = withValidation(result, model)
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
    // <source>.asJson()
    private fun simpleSerialization(source: KotlinExpression) = source.invoke("asJson")


    // produces
    // @JvmName(name = "as<ModelName>FromJson")
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = asString().as<ModelName>()
    override fun MethodAware.installDeserializationFeature(
        model: EnumModelClass, feature: ModelDeserializationFeature
    ) = feature.matches(JsonDeserializationFeature) {
        val methodName = methodNameOf("as", model.name.name)
        kotlinMethod(
            methodName,
            returnType = Library.Maybe.asTypeReference(model.name.asTypeReference().acceptNull()),
            receiverType = Library.Maybe.asTypeReference(Misc.JsonNode.asTypeReference().acceptNull()),
            bodyAsAssignment = true
        ) {
            kotlinAnnotation(Kotlin.JvmName, "name" to "${methodName}FromJson".literal())
            invoke("asString").invoke(methodName).statement()
        }
    }

    // produces
    // fun asJson(): JsonNode = value.asJson()
    override fun MethodAware.installSerializationFeature(model: EnumModelClass, feature: ModelSerializationFeature) =
        feature.matches(JsonSerializationFeature) {
            kotlinMethod("asJson", returnType = Misc.JsonNode.asTypeReference(), bodyAsAssignment = true) {
                "value".identifier().invoke("asJson").statement()
            }
        }

    // produces
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //
    // }
    override fun MethodAware.installDeserializationFeature(
        model: ObjectModelClass, feature: ModelDeserializationFeature
    ) = feature.matches(JsonDeserializationFeature) {
        val methodName = methodNameOf("as", model.name.name)
        kotlinMethod(
            methodName,
            returnType = Library.Maybe.asTypeReference(model.name.asTypeReference().acceptNull()),
            receiverType = Library.Maybe.asTypeReference(Misc.JsonNode.asTypeReference().acceptNull()),
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
    // fun asJson(): JsonNode = objectNode()
    //     .setProperty("<propertyName>", <SerializationStatement for property>, (true|false))
    //     ...
    override fun MethodAware.installSerializationFeature(
        model: ObjectModelClass, feature: ModelSerializationFeature
    ) = feature.matches(JsonSerializationFeature) {
        kotlinMethod("asJson", returnType = Misc.JsonNode.asTypeReference(), bodyAsAssignment = true) {
            var expression = invoke("objectNode")

            model.properties.forEach {
                val defaultValue = it.model.getDefinedDefaultValue()
                val finalModel = it.model.adjustToDefault(defaultValue)

                val propertyExpression = registry.getHandler<SerializationHandler, KotlinExpression> {
                    serializationExpression(it.name.identifier(), finalModel, ContentType.ApplicationJson)
                }

                expression = expression.wrap().invoke(
                    "setProperty",
                    it.sourceName.literal(),
                    propertyExpression,
                    // only check for required, not !nullable, because we want to include null in the response
                    // if the type is nullable but required
                    finalModel.instance.required.literal()
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
                    }.statement()
                }
            }

            expression.statement()
        }
    }

    // produces
    // fun unsafeJson(<properties>): UnsafeJson<<ModelName>> = UnsafeJson(objectNode()
    //     .setProperty("<propertyName>", <SerializationStatement for property>, (true|false))
    //     ...
    override fun MethodAware.installTestSerializationFeature(
        model: ObjectModelClass,
        feature: ModelSerializationFeature
    ) = feature.matches(JsonSerializationFeature) {
        kotlinMethod(
            "unsafeJson",
            returnType = Library.UnsafeJson.asTypeReference(model.name.asTypeReference()),
            bodyAsAssignment = true
        ) {
            var expression = invoke("objectNode")

            model.properties.forEach { property ->
                val propertyModel = property.model.nestedAcceptNull().acceptNull()
                kotlinParameter(property.name, propertyModel.asUnsafeTypeReference(), nullLiteral())

                val serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                    serializationExpression(property.name.identifier(), propertyModel, ContentType.ApplicationJson)
                }

                expression = expression.wrap().invoke(
                    "setProperty",
                    property.sourceName.literal(),
                    serialization,
                    // only check for required, not !nullable, because we want to include null in the response
                    // if the type is nullable but required
                    propertyModel.instance.required.literal()
                )

            }

            model.additionalProperties?.let { additionalProperties ->
                val propertyModel = additionalProperties.nestedAcceptNull().acceptNull()
                val type = Kotlin.Map.asTypeReference(
                    Kotlin.String.asTypeReference(), propertyModel.asUnsafeTypeReference()
                ).acceptNull()
                kotlinParameter("additionalProperties", type, nullLiteral())

                val protectedNames = model.properties.map { it.sourceName.literal() }

                expression = expression.wrap().invoke(
                    "setAdditionalProperties",
                    "additionalProperties".identifier(),
                    *protectedNames.toTypedArray()
                ) {
                    registry.getHandler<SerializationHandler, KotlinExpression> {
                        serializationExpression("it".identifier(), propertyModel, ContentType.ApplicationJson)
                    }.statement()
                }
            }

            invoke(Library.UnsafeJson.identifier(), expression).statement()
        }
    }

    // produces
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //
    // }
    override fun MethodAware.installDeserializationFeature(
        model: OneOfModelClass, feature: ModelDeserializationFeature
    ) = feature.matches(JsonDeserializationFeature) {
        val methodName = methodNameOf("as", model.name.name)
        kotlinMethod(
            methodName,
            returnType = Library.Maybe.asTypeReference(model.name.asTypeReference().acceptNull()),
            receiverType = Library.Maybe.asTypeReference(Misc.JsonNode.asTypeReference().acceptNull()),
            bodyAsAssignment = true
        ) {
            when (val discriminatorProperty = model.discriminator) {
                null -> generateJsonDeserializationWithoutDescriptor(model)
                else -> generateJsonDeserializationWithDescriptor(model, discriminatorProperty.sourceName)
            }
        }
    }

    private fun StatementAware.generateJsonDeserializationWithoutDescriptor(model: OneOfModelClass) {
        // onSuccess instead of onNotNull in case one of the options is nullable. maybe we can find a better way to handle this
        invoke("onSuccess") {
            val statements = model.options.mapIndexed { index, option ->
                val variable = registry.getHandler<DeserializationHandler, KotlinExpression> {
                    deserializationExpression("this".identifier(), option.model, ContentType.ApplicationJson)
                }.declaration("option${index}Maybe")
                variable to option.name
            }

            val maybeParameters = statements.map { it.first.identifier() }
            invoke("maybeOneOf", "context".identifier(), *maybeParameters.toTypedArray()) {
                statements.forEach { (variableName, className) ->
                    variableName.identifier().invoke("doOnSuccess") {
                        invoke(className.identifier(), "it".identifier()).returnStatement("maybeOneOf")
                    }.statement()
                }

                invoke(Kotlin.IllegalStateException.identifier(), "this should never happen".literal())
                    .throwStatement()
            }.statement()
        }.statement()
    }

    private fun StatementAware.generateJsonDeserializationWithDescriptor(
        model: OneOfModelClass, discriminatorProperty: String
    ) {
        // onSuccess instead of onNotNull in case one of the options is nullable. maybe we can find a better way to handle this
        invoke("onNotNull") {
            // renders
            //
            // val discriminator = value.get("<discriminatorName>")?.asText()
            val discriminatorVariable = "value".identifier()
                .invoke("get", discriminatorProperty.literal())
                .nullCheck()
                .invoke("asText")
                .declaration("discriminator")

            whenExpression(discriminatorVariable.identifier()) {
                optionBlock(nullLiteral()) {
                    // renders
                    //
                    // failure(ValidationError("discriminator field '<discriminatorName>' is missing", context, ErrorKind.Invalid))
                    InvocationExpression.invoke(
                        "failure",
                        InvocationExpression.invoke(
                            Library.ValidationError.identifier(),
                            "discriminator field '$discriminatorProperty' is missing".literal(),
                            "context".identifier(),
                            Library.ErrorKind.identifier().property("Invalid")
                        )
                    ).statement()
                }

                model.options.forEach {
                    val aliases = it.aliases.map { alias -> alias.literal() }
                    optionBlock(*aliases.toTypedArray()) {
                        // renders
                        //
                        // this.<DeserializationStatements>
                        //     .onSuccess { success(<ContainerClass>(value)) }
                        val expression = registry.getHandler<DeserializationHandler, KotlinExpression> {
                            deserializationExpression("this".identifier(), it.model, ContentType.ApplicationJson)
                        }

                        expression.wrap()
                            .invoke("onSuccess") {
                                InvocationExpression.invoke(
                                    "success",
                                    InvocationExpression.invoke(it.name.identifier(), "value".identifier())
                                ).statement()
                            }
                            .statement()
                    }
                }

                optionBlock("else".identifier()) {
                    // renders
                    //
                    // failure(ValidationError("discriminator field '<discriminatorName>' has invalid value '$discriminator'", context, ErrorKind.Invalid))
                    InvocationExpression.invoke(
                        "failure",
                        InvocationExpression.invoke(
                            Library.ValidationError.identifier(),
                            "discriminator field '$discriminatorProperty' has invalid value '\$discriminator'".literal(),
                            "context".identifier(),
                            Library.ErrorKind.identifier().property("Invalid")
                        )
                    ).statement()
                }
            }.statement()

        }.statement()
    }

    override fun MethodAware.installSerializationFeature(model: OneOfModelClass, feature: ModelSerializationFeature) =
        feature.matches(JsonSerializationFeature) {
            kotlinMethod("asJson", returnType = Misc.JsonNode.asTypeReference())
        }

    // produces
    // fun is<OptionName>(block: <ModelName>.() -> Unit) = when (this) {
    //     is <OptionName> -> value.apply(block)
    //     else -> throw AssertionFailedError("Assertion failed.", <OptionName>::class.java.name, javaClass.name)
    // }
    override fun MethodAware.installTestSerializationFeature(
        model: OneOfModelClass, feature: ModelSerializationFeature
    ) = feature.matches(JsonSerializationFeature) {
        model.options.forEach { option ->
            kotlinMethod(methodNameOf("is", option.name.name), bodyAsAssignment = true) {
                kotlinParameter(
                    "block",
                    KotlinDelegateTypeReference(option.model.asTypeReference(), Kotlin.Unit.asTypeReference())
                )

                whenExpression("this".identifier()) {
                    optionBlock(AssignableExpression.assignable(option.name.asTypeReference())) {
                        "value".identifier().invoke("apply", "block".identifier()).statement()
                    }
                    optionBlock("else".identifier()) {
                        InvocationExpression.invoke(
                            Misc.AssertionFailedError.identifier(),
                            "Assertion failed.".literal(),
                            option.name.identifier().functionReference("class.java").property("name"),
                            "javaClass".identifier().property("name")
                        ).throwStatement()
                    }
                }.statement()
            }
        }
    }

    // produces
    // fun asJson(): JsonNode = value?.asJson() ?: NullNode.instance
    // or
    // fun asJson(): JsonNode = value.asJson()
    // or if the oneOf has a discriminator
    // fun asJson(): JsonNode = value.copy(<discriminatorProperty> = "<alias>").asJson()
    override fun MethodAware.installSerializationFeature(
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

    // produces
    // fun unsafeJson(value: UnsafeJson<<ModelName>>? = null): UnsafeJson<<OptionName>> = UnsafeJson((value?.value ?: NullNode.instance))
    override fun MethodAware.installTestSerializationFeature(
        model: OneOfModelOption, discriminatorProperty: String?, feature: ModelSerializationFeature
    ) = feature.matches(JsonSerializationFeature) {

        kotlinMethod(
            "unsafeJson",
            returnType = Library.UnsafeJson.asTypeReference(model.name.asTypeReference()),
            bodyAsAssignment = true
        ) {
            val optionModel = model.model.nestedAcceptNull().acceptNull()
            kotlinParameter("value", optionModel.asUnsafeTypeReference(), expression = nullLiteral())

            var serialization = registry.getHandler<SerializationHandler, KotlinExpression> {
                serializationExpression("value".identifier(), optionModel, ContentType.ApplicationJson)
            }

            serialization = serialization.nullFallback(Misc.NullNode.staticContext().property("instance"))

            invoke(Library.UnsafeJson.identifier(), serialization).statement()
        }
    }

    private fun ModelUsage.hasRealDefaultValueDeclared() = when (instance) {
        is EnumModelInstance -> instance.defaultValue != null
        is PrimitiveTypeModelInstance -> instance.defaultValue != null
        else -> false
    }

    private fun ModelUsage.nestedAcceptNull(): ModelUsage = when (instance) {
        is CollectionModelInstance -> instance.copy(items = instance.items.nestedAcceptNull().acceptNull())
        is EnumModelInstance -> instance
        is MapModelInstance -> instance.copy(items = instance.items.nestedAcceptNull().acceptNull())
        is ObjectModelInstance -> instance
        is OneOfModelInstance -> instance
        is PrimitiveTypeModelInstance -> instance
    }.run {
        ModelUsage(this)
    }

}