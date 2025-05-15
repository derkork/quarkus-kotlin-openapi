package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiSpec

class SpecInspection(val spec: OpenApiSpec) {

    fun bundles(block: RequestBundleInspection.() -> Unit) =
        spec.bundles.forEach { RequestBundleInspection(it).block() }

    fun schemas(block: SchemaInspection.() -> Unit) =
        spec.schemas.forEach { SchemaInspection(it).block() }

}

fun OpenApiSpec.inspect(block: SpecInspection.() -> Unit) = SpecInspection(this).block()


