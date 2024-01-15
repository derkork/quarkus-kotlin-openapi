package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle

class RequestBundleInspection(val bundle: TransformableRequestBundle) {

    fun requests(block: RequestInspection.() -> Unit) = bundle.requests.forEach { RequestInspection(it).block() }

}