package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DependencyContainerFeatureHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DependencyContainerFeatureHandlerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Misc
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.asTypeReference
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyContainer
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyContainerHandler

class JsonDependencyHandler : DependencyContainerHandler, DependencyContainerFeatureHandler {

    override fun registerDependencies(dependencyContainer: DependencyContainer, model: ModelUsage, contentType: ContentType) =
        contentType.matches(ContentType.ApplicationJson) {
            dependencyContainer.features += ObjectMapperFeature
        }

    override fun DependencyContainerFeatureHandlerContext.installDependency(feature: Feature) =
        feature.matches(ObjectMapperFeature) {
            installDefaultDependency("objectMapper", Misc.ObjectMapper.asTypeReference())
        }

}