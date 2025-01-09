package com.ancientlightstudios.quarkus.kotlin.openapi.handler.json

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DependencyVogelEmitter.Companion.emitDefaultDependencyVogelMember
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.DependencyVogelFeatureHandler
import com.ancientlightstudios.quarkus.kotlin.openapi.handler.Feature
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinClass
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Misc
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.asTypeReference

class ObjectMapperHandler : DependencyVogelFeatureHandler {

    override fun canHandleFeature(feature: Feature) = feature is ObjectMapperFeature

    override fun KotlinClass.installFeature(feature: Feature) {
        emitDefaultDependencyVogelMember("objectMapper", Misc.ObjectMapper.asTypeReference())
    }

}