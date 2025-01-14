package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.*

class ObjectModelClassEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<ObjectModelClass>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(model: ObjectModelClass) {
        kotlinFile(model.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinClass(name, asDataClass = true) {
                var additionalConstructor: KotlinConstructor? = null

                if (model.needsPropertiesCount) {
                    interfaces += Library.PropertiesContainer.asTypeReference()
                    constructorAccessModifier = KotlinAccessModifier.Private

                    // create an additional member that contains the number of received properties
                    kotlinMember(
                        "receivedPropertiesCount",
                        Kotlin.Int.asTypeReference(),
                        accessModifier = KotlinAccessModifier.Private
                    )

                    additionalConstructor = kotlinConstructor {
                        // initialize the `receivedPropertiesCount` member with -1 if this ctor is called
                        addPrimaryConstructorParameter((-1).literal())
                    }

                    generatePropertyCountMethod()
                }

                model.properties.forEach {
                    val defaultValue = it.model.getDefaultValue()
                    val finalModel = it.model.adjustToDefault(defaultValue)
                    kotlinMember(
                        it.name,
                        finalModel.asTypeReference(),
                        accessModifier = null,
                        default = defaultValue.toKotlinExpression()
                    )

                    // if the second ctor exists, add the property there as well
                    additionalConstructor?.apply {
                        kotlinParameter(it.name, finalModel.asTypeReference(), defaultValue.toKotlinExpression())
                        // and pass it to the primary ctor
                        addPrimaryConstructorParameter(it.name.identifier())
                    }
                }

                model.additionalProperties?.let {
                    kotlinMember(
                        "additionalProperties",
                        Kotlin.Map.asTypeReference(Kotlin.String.asTypeReference(), it.asTypeReference()),
                        accessModifier = null,
                        default = invoke("mapOf")
                    )

                    // if the second ctor exists, add the map there as well
                    additionalConstructor?.apply {
                        kotlinParameter(
                            "additionalProperties",
                            Kotlin.Map.asTypeReference(Kotlin.String.asTypeReference(), it.asTypeReference()),
                            invoke("mapOf")
                        )
                        // and pass it to the primary ctor
                        addPrimaryConstructorParameter("additionalProperties".identifier())
                    }
                }

                model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                    getHandler<ObjectModelSerializationHandler, Unit> {
                        installSerializationFeature(model, feature)
                    }
                }

                kotlinCompanion {
                    model.features.filterIsInstance<ModelDeserializationFeature>().forEach { feature ->
                        getHandler<ObjectModelDeserializationHandler, Unit> {
                            installDeserializationFeature(model, feature)
                        }
                    }

                    // TODO
//                    if (withTestSupport) {
//                        generateUnsafeMethods(spec.serializationDirection)
//                    }

                }
            }
        }
    }

    private fun ModelUsage.getDefaultValue(): DefaultValue = when (val instance = this.instance) {
        is CollectionModelInstance -> DefaultValue.nullOrNone(isNullable())
        is EnumModelInstance -> when (instance.defaultValue) {
            null -> DefaultValue.nullOrNone(isNullable())
            else -> DefaultValue.EnumValue(instance.ref, instance.defaultValue)
        }

        is MapModelInstance -> DefaultValue.nullOrNone(isNullable())
        is ObjectModelInstance -> DefaultValue.nullOrNone(isNullable())
        is OneOfModelInstance -> DefaultValue.nullOrNone(isNullable())
        is PrimitiveTypeModelInstance -> when (instance.defaultValue) {
            null -> DefaultValue.nullOrNone(isNullable())
            else -> DefaultValue.StaticValue(instance.itemType, instance.defaultValue)
        }
    }

    // produces
    // override fun receivedPropertiesCount() = receivedPropertiesCount
    private fun KotlinClass.generatePropertyCountMethod() {
        kotlinMethod("receivedPropertiesCount", override = true, bodyAsAssignment = true) {
            "receivedPropertiesCount".identifier().statement()
        }
    }

}

interface ObjectModelSerializationHandler : Handler {

    fun KotlinClass.installSerializationFeature(model: ObjectModelClass, feature: ModelSerializationFeature):
            HandlerResult<Unit>

}

interface ObjectModelDeserializationHandler : Handler {

    fun KotlinCompanion.installDeserializationFeature(model: ObjectModelClass, feature: ModelDeserializationFeature):
            HandlerResult<Unit>

}


//    private fun KotlinCompanion.generateUnsafeMethods(serializationDirection: Direction) {
//        val types = typeDefinition.getContentTypes(serializationDirection)
//        if (types.contains(ContentType.ApplicationJson)) {
//            generateJsonUnsafeMethod()
//        }
//    }
//
//    private fun KotlinCompanion.generateJsonUnsafeMethod() {
//        kotlinMethod(
//            "unsafeJson".methodName(),
//            returnType = Library.UnsafeJsonClass.typeName().of(typeDefinition.modelName.typeName()),
//            bodyAsAssignment = true
//        ) {
//            typeDefinition.properties.forEach {
//                val defaultValue = generateDefaultValueExpression(it.typeUsage, nullLiteral())
//                kotlinParameter(
//                    it.name,
//                    it.typeUsage.buildUnsafeJsonType(),
//                    expression = defaultValue
//                )
//
//            }
//
//            var expression = invoke("objectNode".rawMethodName())
//
//            typeDefinition.properties.forEach {
//                val serialization = emitterContext.runEmitter(
//                    UnsafeSerializationStatementEmitter(it.typeUsage, it.name, ContentType.ApplicationJson)
//                ).resultStatement
//
//                expression = expression.wrap().invoke(
//                    "setProperty".rawMethodName(),
//                    it.sourceName.literal(),
//                    serialization,
//                    // only check for required, not !nullable, because we want to include null in the response
//                    // if the type is nullable but required
//                    it.typeUsage.required.literal()
//                )
//            }
//
//            typeDefinition.additionalProperties?.let {
//                kotlinParameter(
//                    "additionalProperties".variableName(),
//                    Kotlin.MapClass.typeName(true).of(Kotlin.StringClass.typeName(), it.buildUnsafeJsonType()),
//                    expression = nullLiteral()
//                )
//
//                val protectedNames = typeDefinition.properties.map { it.sourceName.literal() }
//
//                expression = expression.wrap().invoke(
//                    "setAdditionalProperties".rawMethodName(),
//                    "additionalProperties".variableName(),
//                    *protectedNames.toTypedArray()
//                ) {
//                    emitterContext.runEmitter(
//                        UnsafeSerializationStatementEmitter(it, "it".variableName(), ContentType.ApplicationJson)
//                    ).resultStatement.statement()
//                }
//            }
//
//            invoke(Library.UnsafeJsonClass.constructorName, expression).statement()
//        }
//    }
//
//}