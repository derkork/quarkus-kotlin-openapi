package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.IdentifierExpression.Companion.identifier
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.InvocationExpression.Companion.invoke
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.OneOfModelClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.OneOfModelOption

class OneOfModelClassEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<OneOfModelClass>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(model: OneOfModelClass) {
        kotlinFile(model.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinInterface(name, sealed = true) {
                model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                    getHandler<OneOfModelSerializationHandler, Unit> {
                        installSerializationFeature(model, feature)
                    }
                }

                if (withTestSupport) {
                    model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                        getHandler<OneOfModelSerializationHandler, Unit> {
                            installTestSerializationFeature(model, feature)
                        }
                    }
                }

                kotlinCompanion {
                    model.options.forEach {
                        kotlinMethod("of", bodyAsAssignment = true) {
                            kotlinParameter("value", it.model.asTypeReference())
                            invoke(it.name.identifier(), "value".identifier()).statement()
                        }
                    }

                    model.features.filterIsInstance<ModelDeserializationFeature>().forEach { feature ->
                        getHandler<OneOfModelDeserializationHandler, Unit> {
                            installDeserializationFeature(model, feature)
                        }
                    }
                }
            }

            model.options.forEach {
                kotlinClass(it.name.asTypeName(), asDataClass = true, baseClass = KotlinBaseClass(name)) {
                    kotlinMember("value", it.model.asTypeReference(), accessModifier = null)

                    model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                        getHandler<OneOfModelSerializationHandler, Unit> {
                            installSerializationFeature(it, model.discriminator?.name, feature)
                        }
                    }

                    kotlinCompanion {
                        if (withTestSupport) {
                            model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                                getHandler<OneOfModelSerializationHandler, Unit> {
                                    installTestSerializationFeature(it, model.discriminator?.name, feature)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

interface OneOfModelSerializationHandler : Handler {

    fun MethodAware.installSerializationFeature(model: OneOfModelClass, feature: ModelSerializationFeature):
            HandlerResult<Unit>

    fun MethodAware.installTestSerializationFeature(model: OneOfModelClass, feature: ModelSerializationFeature):
            HandlerResult<Unit>

    fun MethodAware.installSerializationFeature(
        model: OneOfModelOption, discriminatorProperty: String?, feature: ModelSerializationFeature
    ): HandlerResult<Unit>

    fun MethodAware.installTestSerializationFeature(
        model: OneOfModelOption, discriminatorProperty: String?, feature: ModelSerializationFeature
    ): HandlerResult<Unit>

}

interface OneOfModelDeserializationHandler : Handler {

    fun MethodAware.installDeserializationFeature(model: OneOfModelClass, feature: ModelDeserializationFeature):
            HandlerResult<Unit>

}
