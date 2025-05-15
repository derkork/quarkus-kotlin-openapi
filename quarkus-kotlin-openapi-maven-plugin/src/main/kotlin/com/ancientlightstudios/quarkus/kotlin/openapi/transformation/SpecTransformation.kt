package com.ancientlightstudios.quarkus.kotlin.openapi.transformation

interface SpecTransformation {

    fun TransformationContext.perform()

}