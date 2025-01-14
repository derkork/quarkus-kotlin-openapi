package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyVogel
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyVogelHandler

class JsonDependenciesHandler : DependencyVogelHandler {

    override fun installFeatureFor(dependencyVogel: DependencyVogel, contentType: ContentType) =
        contentType.matches(ContentType.ApplicationJson) {
            dependencyVogel.features += ObjectMapperFeature
        }

}