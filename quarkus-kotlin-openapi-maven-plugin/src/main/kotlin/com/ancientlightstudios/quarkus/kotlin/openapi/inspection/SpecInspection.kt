package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class SpecInspection(val spec: TransformableSpec) {

    fun bundles(block: RequestBundleInspection.() -> Unit) =
        spec.bundles.forEach { RequestBundleInspection(it).block() }

}

fun TransformableSpec.inspect(block: SpecInspection.() -> Unit) = SpecInspection(this).block()


