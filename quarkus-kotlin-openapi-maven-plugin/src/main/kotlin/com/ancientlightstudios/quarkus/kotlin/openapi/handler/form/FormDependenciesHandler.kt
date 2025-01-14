package com.ancientlightstudios.quarkus.kotlin.openapi.handler.form

import com.ancientlightstudios.quarkus.kotlin.openapi.handler.matches
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ContentType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.DependencyVogel
import com.ancientlightstudios.quarkus.kotlin.openapi.transformation.DependencyVogelHandler

class FormDependenciesHandler : DependencyVogelHandler {

    override fun installFeatureFor(dependencyVogel: DependencyVogel, contentType: ContentType) =
        contentType.matches(ContentType.ApplicationFormUrlencoded) {
            // TODO: based on the type of the property models or the content encoding, ask other handler to install features e.g. json
        }

}