package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Handler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerResult
import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.SolutionHint.solution
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.*
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.EnumModelClass

class EnumModelClassEmitter : CodeEmitter {

    override fun EmitterContext.emit() {
        spec.solution.files
            .filterIsInstance<EnumModelClass>()
            .forEach { emitFile(it) }
    }

    private fun EmitterContext.emitFile(model: EnumModelClass) {
        kotlinFile(model.name.asTypeName()) {
            registerImports(Library.All)
            registerImports(config.additionalImports())

            kotlinEnum(name) {
                kotlinMember(
                    "value",
                    model.itemType.asTypeReference(),
                    accessModifier = null
                )

                model.items.forEach {
                    kotlinEnumItem(it.name, model.itemType.literalFor(it.value))
                }

                model.features.filterIsInstance<ModelSerializationFeature>().forEach { feature ->
                    getHandler<EnumModelSerializationHandler, Unit> {
                        installSerializationFeature(model, feature)
                    }
                }

                kotlinCompanion {
                    model.features.filterIsInstance<ModelDeserializationFeature>().forEach { feature ->
                        getHandler<EnumModelDeserializationHandler, Unit> {
                            installDeserializationFeature(model, feature)
                        }
                    }
                }
            }
        }
    }

}

interface EnumModelSerializationHandler : Handler {

    fun MethodAware.installSerializationFeature(model: EnumModelClass, feature: ModelSerializationFeature):
            HandlerResult<Unit>

}

interface EnumModelDeserializationHandler : Handler {

    fun MethodAware.installDeserializationFeature(model: EnumModelClass, feature: ModelDeserializationFeature):
            HandlerResult<Unit>

}
