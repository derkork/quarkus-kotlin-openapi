package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.CombineIntoObjectStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.UnsafeSerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DeserializationDirectionHint.deserializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SerializationDirectionHint.serializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.components.PropertiesValidation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class ObjectModelClassEmitter(private val typeDefinition: ObjectTypeDefinition, private val withTestSupport: Boolean) :
    CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this

        kotlinFile(typeDefinition.modelName) {
            registerImports(Library.AllClasses)
            registerImports(getAdditionalImports())

            val hasPropertiesValidation = typeDefinition.validations.any { it is PropertiesValidation }

            val baseInterfaces = mutableListOf<ClassName>()
            var constructorModifier: KotlinAccessModifier? = null

            if (hasPropertiesValidation) {
                baseInterfaces.add(Library.PropertiesContainerInterface)
                constructorModifier = KotlinAccessModifier.Private
            }

            kotlinClass(
                fileName,
                asDataClass = true,
                interfaces = baseInterfaces,
                constructorAccessModifier = constructorModifier
            ) {
                if (hasPropertiesValidation) {
                    kotlinMember(
                        "receivedPropertiesCount".variableName(),
                        Kotlin.IntClass.typeName(),
                        accessModifier = KotlinAccessModifier.Private
                    )
                    kotlinConstructor {
                        addPrimaryConstructorParameter((-1).literal())

                        typeDefinition.properties.forEach {
                            val defaultValue = generateDefaultValueExpression(it.typeUsage)
                            kotlinParameter(
                                it.name,
                                it.typeUsage.buildValidType(),
                                expression = defaultValue
                            )
                            addPrimaryConstructorParameter(it.name)
                        }

                        typeDefinition.additionalProperties?.let {
                            kotlinParameter(
                                "additionalProperties".variableName(),
                                Kotlin.MapClass.typeName().of(Kotlin.StringClass.typeName(), it.buildValidType()),
                                expression = invoke("mapOf".methodName())
                            )
                            addPrimaryConstructorParameter("additionalProperties".variableName())
                        }
                    }

                    generatePropertyCountMethod()
                }

                typeDefinition.properties.forEach {
                    val defaultValue = generateDefaultValueExpression(it.typeUsage)
                    kotlinMember(
                        it.name,
                        it.typeUsage.buildValidType(),
                        accessModifier = null,
                        default = defaultValue
                    )
                }

                typeDefinition.additionalProperties?.let {
                    kotlinMember(
                        "additionalProperties".variableName(),
                        Kotlin.MapClass.typeName().of(Kotlin.StringClass.typeName(), it.buildValidType()),
                        accessModifier = null,
                        default = invoke("mapOf".methodName())
                    )
                }

                generateSerializeMethods(spec.serializationDirection)

                kotlinCompanion {
                    generateDeserializeMethods(spec.deserializationDirection, hasPropertiesValidation)

                    if (withTestSupport) {
                        generateUnsafeMethods(spec.serializationDirection)
                    }
                }

            }
        }.writeFile()
    }

    private fun KotlinClass.generatePropertyCountMethod() {
        kotlinMethod("receivedPropertiesCount".methodName(), override = true, bodyAsAssignment = true) {
            "receivedPropertiesCount".variableName().statement()
        }
    }

    private fun KotlinClass.generateSerializeMethods(serializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(serializationDirection)
        if (types.contains(ContentType.ApplicationJson)) {
            generateJsonSerializeMethod()
        }
    }

    private fun KotlinCompanion.generateDeserializeMethods(
        deserializationDirection: Direction,
        hasPropertyValidation: Boolean
    ) {
        val types = typeDefinition.getContentTypes(deserializationDirection)
            .intersect(listOf(ContentType.ApplicationJson))

        if (types.isNotEmpty()) {
            if (types.contains(ContentType.ApplicationJson)) {
                generateJsonDeserializeMethod(hasPropertyValidation)
            }
        }
    }

    // generates a method like this
    //
    // fun asJson(): JsonNode = objectNode()
    //     .setProperty("<propertyName>", <SerializationStatement for property>, (true|false))
    //
    // generates a call to setProperty for every property of the object
    private fun MethodAware.generateJsonSerializeMethod() {
        kotlinMethod("asJson".rawMethodName(), returnType = Misc.JsonNodeClass.typeName(), bodyAsAssignment = true) {
            var expression = invoke("objectNode".rawMethodName())

            typeDefinition.properties.forEach {
                val serialization = emitterContext.runEmitter(
                    SerializationStatementEmitter(it.typeUsage, it.name, ContentType.ApplicationJson)
                ).resultStatement

                expression = expression.wrap().invoke(
                    "setProperty".rawMethodName(),
                    it.sourceName.literal(),
                    serialization,
                    // only check for required, not !nullable, because we want to include null in the response
                    // if the type is nullable but required
                    it.typeUsage.required.literal()
                )
            }

            typeDefinition.additionalProperties?.let {
                val protectedNames = typeDefinition.properties.map { it.sourceName.literal() }

                expression = expression.wrap().invoke(
                    "setAdditionalProperties".rawMethodName(),
                    "additionalProperties".variableName(),
                    *protectedNames.toTypedArray()
                ) {
                    emitterContext.runEmitter(
                        SerializationStatementEmitter(it, "it".variableName(), ContentType.ApplicationJson)
                    ).resultStatement.statement()
                }
            }

            expression.statement()
        }
    }

    // generates a method like this
    //
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //
    // }
    //
    // there is an option for every enum value
    private fun MethodAware.generateJsonDeserializeMethod(hasPropertyValidation: Boolean) {
        kotlinMethod(
            typeDefinition.modelName.value.methodName(prefix = "as"),
            returnType = Library.MaybeClass.typeName().of(typeDefinition.modelName.typeName(true)),
            receiverType = Library.MaybeClass.typeName().of(Misc.JsonNodeClass.typeName(true)),
            bodyAsAssignment = true
        ) {
            invoke("onNotNull".rawMethodName()) {
                if (hasPropertyValidation) {
                    val propertiesWithDefault = typeDefinition.properties.filter { hasRealDefaultValueDeclared(it.typeUsage) }.map { it.sourceName.literal() }
                    invoke("listOf<String>".rawMethodName(), propertiesWithDefault).declaration("propertiesWithDefault")

                    if (typeDefinition.additionalProperties != null) {
                        // just count all properties
                        "value".variableName().invoke("countAllProperties".methodName(), "propertiesWithDefault".variableName())
                            .declaration("propertyCount")
                    } else {
                        // only count known properties
                        val knownProperties = typeDefinition.properties.map { it.sourceName.literal() }
                        invoke("listOf<String>".rawMethodName(), knownProperties).declaration("knownProperties")
                        "value".variableName().invoke("countKnownProperties".methodName(), "knownProperties".variableName(), "propertiesWithDefault".variableName())
                            .declaration("propertyCount")
                    }
                }

                // iterate over all members and create a deserialize statement for each
                val root = "value".variableName()
                val objectParts = typeDefinition.properties.mapTo(mutableListOf()) {

                    val statement = root.invoke(
                        "findProperty".rawMethodName(),
                        it.sourceName.literal(),
                        "\${context}.${it.sourceName}".literal()
                    )

                    emitterContext.runEmitter(
                        DeserializationStatementEmitter(it.typeUsage, statement, ContentType.ApplicationJson, false)
                    ).resultStatement.declaration("${it.sourceName}Maybe")
                }

                typeDefinition.additionalProperties?.let {
                    val protectedNames = typeDefinition.properties.map { it.sourceName.literal() }
                    val maybe = invoke("propertiesAsMap".rawMethodName(), *protectedNames.toTypedArray()) {
                        emitterContext.runEmitter(
                            DeserializationStatementEmitter(it, "it".variableName(), ContentType.ApplicationJson, false)
                        ).resultStatement.statement()
                    }
                        .invoke("required".rawMethodName())
                        .declaration("additionalPropertiesMaybe".variableName())

                    objectParts.add(maybe)
                }

                val additionalProperties = when (hasPropertyValidation) {
                    true -> listOf("propertyCount".variableName())
                    false -> listOf()
                }

                if (objectParts.isEmpty()) {
                    // just return a new instance
                    invoke(typeDefinition.modelName.constructorName, additionalProperties).statement()
                } else {
                    emitterContext.runEmitter(
                        CombineIntoObjectStatementEmitter(
                            "context".variableName(), typeDefinition.modelName, additionalProperties, objectParts
                        )
                    ).resultStatement?.statement()
                }
            }.statement()
        }
    }

    private fun hasRealDefaultValueDeclared(typeUsage: TypeUsage) = when (val safeType = typeUsage.type) {
        is PrimitiveTypeDefinition -> safeType.defaultValue != null
        is EnumTypeDefinition -> safeType.defaultValue != null
        else -> false
    }

    private fun generateDefaultValueExpression(
        typeUsage: TypeUsage,
        fallback: KotlinExpression? = null
    ): KotlinExpression? {
        val declaredDefaultValue = when (val safeType = typeUsage.type) {
            is PrimitiveTypeDefinition -> safeType.defaultExpression()
            is EnumTypeDefinition -> safeType.defaultExpression()
            is CollectionTypeDefinition,
            is ObjectTypeDefinition,
            is OneOfTypeDefinition -> null
        }

        // if there is a default expression defined, use it. Otherwise, use the null expression, if null is allowed or the fallback
        return declaredDefaultValue ?: if (typeUsage.isNullable()) nullLiteral() else fallback
    }

    private fun KotlinCompanion.generateUnsafeMethods(serializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(serializationDirection)
        if (types.contains(ContentType.ApplicationJson)) {
            generateJsonUnsafeMethod()
        }
    }

    private fun KotlinCompanion.generateJsonUnsafeMethod() {
        kotlinMethod(
            "unsafeJson".methodName(),
            returnType = Library.UnsafeJsonClass.typeName().of(typeDefinition.modelName.typeName()),
            bodyAsAssignment = true
        ) {
            typeDefinition.properties.forEach {
                val defaultValue = generateDefaultValueExpression(it.typeUsage, nullLiteral())
                kotlinParameter(
                    it.name,
                    it.typeUsage.buildUnsafeJsonType(),
                    expression = defaultValue
                )

            }

            var expression = invoke("objectNode".rawMethodName())

            typeDefinition.properties.forEach {
                val serialization = emitterContext.runEmitter(
                    UnsafeSerializationStatementEmitter(it.typeUsage, it.name, ContentType.ApplicationJson)
                ).resultStatement

                expression = expression.wrap().invoke(
                    "setProperty".rawMethodName(),
                    it.sourceName.literal(),
                    serialization,
                    // only check for required, not !nullable, because we want to include null in the response
                    // if the type is nullable but required
                    it.typeUsage.required.literal()
                )
            }

            typeDefinition.additionalProperties?.let {
                kotlinParameter(
                    "additionalProperties".variableName(),
                    Kotlin.MapClass.typeName(true).of(Kotlin.StringClass.typeName(), it.buildUnsafeJsonType()),
                    expression = nullLiteral()
                )

                val protectedNames = typeDefinition.properties.map { it.sourceName.literal() }

                expression = expression.wrap().invoke(
                    "setAdditionalProperties".rawMethodName(),
                    "additionalProperties".variableName(),
                    *protectedNames.toTypedArray()
                ) {
                    emitterContext.runEmitter(
                        UnsafeSerializationStatementEmitter(it, "it".variableName(), ContentType.ApplicationJson)
                    ).resultStatement.statement()
                }
            }

            invoke(Library.UnsafeJsonClass.constructorName, expression).statement()
        }
    }

}