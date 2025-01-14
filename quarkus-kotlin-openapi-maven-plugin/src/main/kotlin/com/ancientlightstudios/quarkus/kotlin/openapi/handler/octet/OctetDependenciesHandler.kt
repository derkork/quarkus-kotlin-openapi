package com.ancientlightstudios.quarkus.kotlin.openapi.handler.octet

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyVogel
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyVogelHandler

class OctetDependenciesHandler : DependencyVogelHandler {

    override fun installFeatureFor(dependencyVogel: DependencyVogel, contentType: ContentType) =
        // we still have to check if we are allowed/required to act
        contentType.matches(ContentType.ApplicationOctetStream) {
            // nothing to do
        }

}