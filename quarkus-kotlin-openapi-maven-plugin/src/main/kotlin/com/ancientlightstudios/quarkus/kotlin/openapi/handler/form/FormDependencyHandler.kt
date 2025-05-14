package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.HandlerRegistry
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyContainer
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ObjectModelInstance
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyContainerHandler

class FormDependencyHandler : DependencyContainerHandler {

    private lateinit var registry: HandlerRegistry

    override fun initializeContext(registry: HandlerRegistry) {
        this.registry = registry
    }

    override fun registerDependencies(dependencyContainer: DependencyContainer, model: ModelUsage, contentType: ContentType) =
        contentType.matches(ContentType.ApplicationFormUrlencoded) {
            val instance = model.instance
            if (instance is ObjectModelInstance) {
                // explode the form into several parts, which can require different content types
                instance.ref.properties.forEach { property ->
                    installFeatureFor(dependencyContainer, property.model)
                }
            } else {
                // it's just a single item
                installFeatureFor(dependencyContainer, model)
            }
        }

    private fun installFeatureFor(dependencyContainer: DependencyContainer, model: ModelUsage) {
        registry.getHandler<DependencyContainerHandler, Unit> {
            registerDependencies(dependencyContainer, model, model.defaultContentType())
        }
    }
}