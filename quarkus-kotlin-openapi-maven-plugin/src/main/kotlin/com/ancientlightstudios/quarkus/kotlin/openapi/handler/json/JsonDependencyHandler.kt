package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DependencyVogelFeatureHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DependencyVogelFeatureHandlerContext
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Misc
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.asTypeReference
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyVogel
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ModelUsage
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyVogelHandler

class JsonDependencyHandler : DependencyVogelHandler, DependencyVogelFeatureHandler {

    override fun registerDependencies(dependencyVogel: DependencyVogel, model: ModelUsage, contentType: ContentType) =
        contentType.matches(ContentType.ApplicationJson) {
            dependencyVogel.features += ObjectMapperFeature
        }

    override fun DependencyVogelFeatureHandlerContext.installDependency(feature: Feature) =
        feature.matches(ObjectMapperFeature) {
            installDefaultDependency("objectMapper", Misc.ObjectMapper.asTypeReference())
        }

}