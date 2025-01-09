package com.ancientlightstudios.quarkus.kotlin.openapi.handler

interface FeatureHandler : Handler {

    fun canHandleFeature(feature: Feature): Boolean

}