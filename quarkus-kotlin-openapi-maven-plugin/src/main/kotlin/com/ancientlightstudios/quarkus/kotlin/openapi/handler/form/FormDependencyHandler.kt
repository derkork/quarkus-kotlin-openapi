package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyVogel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ObjectModelInstance
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyVogelHandler

class FormDependencyHandler : DependencyVogelHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun registerDependencies(dependencyVogel: DependencyVogel, model: ModelUsage, contentType: ContentType) =
        contentType.matches(ContentType.ApplicationFormUrlencoded) {
            val instance = model.instance
            if (instance is ObjectModelInstance) {
                // explode the form into several parts, which can require different content types
                instance.ref.properties.forEach { property ->
                    installFeatureFor(dependencyVogel, property.model)
                }
            } else {
                // it's just a single item
                installFeatureFor(dependencyVogel, model)
            }
        }

    private fun installFeatureFor(dependencyVogel: DependencyVogel, model: ModelUsage) {
        registry.getHandler<DependencyVogelHandler, Unit> {
            registerDependencies(dependencyVogel, model, model.defaultContentType())
        }
    }
}