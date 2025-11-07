package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ObjectModelClass

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

            kotlinClass(name, asDataClass = model.properties.isNotEmpty()) {
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
                    val defaultValue = it.model.getDefinedDefaultValue()
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

                    if (withTestSupport) {
                        model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                            getHandler<ObjectModelSerializationHandler, Unit> {
                                installTestSerializationFeature(model, feature)
                            }
                        }
                    }
                }
            }
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

    fun MethodAware.installSerializationFeature(model: ObjectModelClass, feature: ModelSerializationFeature):
            HandlerResult<Unit>

    fun MethodAware.installTestSerializationFeature(model: ObjectModelClass, feature: ModelSerializationFeature):
            HandlerResult<Unit>

}

interface ObjectModelDeserializationHandler : Handler {

    fun MethodAware.installDeserializationFeature(model: ObjectModelClass, feature: ModelDeserializationFeature):
            HandlerResult<Unit>

}
