package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.CombineIntoObjectStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.deserialization.DeserializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.serialization.SerializationStatementEmitter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.DeserializationDirectionHint.deserializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SerializationDirectionHint.serializationDirection
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.TypeDefinitionHint.typeDefinition
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.methodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.GenericTypeName.Companion.of
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.TypeName.SimpleTypeName.Companion.typeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.types.*

class DefaultObjectModelClassEmitter(private val typeDefinition: ObjectTypeDefinition) : CodeEmitter {

    private lateinit var emitterContext: EmitterContext

    override fun EmitterContext.emit() {
        emitterContext = this

        kotlinFile(typeDefinition.modelName) {
            registerImports(Library.AllClasses)
            registerImports(getAdditionalImports())

            kotlinClass(fileName, asDataClass = true) {
                typeDefinition.properties.forEach {
                    val forceNullable = !typeDefinition.required.contains(it.sourceName)
                    val defaultValue = generateDefaultValueExpression(it.schema.typeDefinition, forceNullable)
                    kotlinMember(
                        it.name,
                        it.schema.typeDefinition.buildValidType(forceNullable),
                        accessModifier = null,
                        default = defaultValue
                    )
                }

                generateSerializeMethods(spec.serializationDirection)
                generateDeserializeMethods(spec.deserializationDirection)

            }
        }.writeFile()
    }

    private fun KotlinClass.generateSerializeMethods(serializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(serializationDirection)
        if (types.contains(ContentType.ApplicationJson)) {
            generateJsonSerializeMethod()
        }
    }

    private fun KotlinClass.generateDeserializeMethods(deserializationDirection: Direction) {
        val types = typeDefinition.getContentTypes(deserializationDirection)
            .intersect(listOf(ContentType.ApplicationJson))

        if (types.isNotEmpty()) {
            kotlinCompanion {
                if (types.contains(ContentType.ApplicationJson)) {
                    generateJsonDeserializeMethod()
                }
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
            val required = typeDefinition.required

            var expression = invoke("objectNode".rawMethodName())

            typeDefinition.properties.forEach {
                val isPropertyRequired = required.contains(it.sourceName)
                val serialization = emitterContext.runEmitter(
                    SerializationStatementEmitter(
                        it.schema.typeDefinition, !isPropertyRequired, it.name, ContentType.ApplicationJson
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

    // generates a method like this
    //
    // fun Maybe<JsonNode?>.as<ModelName>(): Maybe<<ModelName>?> = onNotNull {
    //
    // }
    //
    // there is an option for every enum value
    private fun MethodAware.generateJsonDeserializeMethod() {
        kotlinMethod(
            typeDefinition.modelName.value.methodName(prefix = "as"),
            returnType = Library.MaybeClass.typeName().of(typeDefinition.modelName, true),
            receiverType = Library.MaybeClass.typeName().of(Misc.JsonNodeClass, true),
            bodyAsAssignment = true
        ) {
            invoke("onNotNull".rawMethodName()) {
                // iterate over all members and create a deserialize statement for each
                val required = typeDefinition.required
                val root = "value".variableName()
                val objectParts = typeDefinition.properties.map {
                    val isPropertyRequired = required.contains(it.sourceName)

                    val statement = root.invoke(
                        "findProperty".rawMethodName(),
                        it.sourceName.literal(),
                        "\${context}.${it.sourceName}".literal()
                    )

                    emitterContext.runEmitter(
                        DeserializationStatementEmitter(
                            it.schema.typeDefinition, !isPropertyRequired, statement, ContentType.ApplicationJson, false
                        )
                    ).resultStatement.assignment("${it.sourceName}Maybe".variableName())
                }

                if (objectParts.isEmpty()) {
                    // just return a new instance
                    invoke(typeDefinition.modelName.constructorName).statement()
                } else {
                    emitterContext.runEmitter(
                        CombineIntoObjectStatementEmitter(
                            "context".variableName(), typeDefinition.modelName, objectParts
                        )
                    ).resultStatement?.statement()
                }
            }.statement()
        }
    }

    private fun generateDefaultValueExpression(
        propertyType: TypeDefinition, forceNullable: Boolean
    ): KotlinExpression? {
        val declaredDefaultValue = when (propertyType) {
            is PrimitiveTypeDefinition -> propertyType.defaultValue
            is EnumTypeDefinition -> propertyType.defaultValue
            is CollectionTypeDefinition,
            is ObjectTypeDefinition -> null
        }

        val canBeNull = forceNullable || propertyType.nullable
        // if there is a default expression defined, use it. Otherwise, use the null expression, if null is allowed
        return declaredDefaultValue ?: if (canBeNull) nullLiteral() else null
    }

}