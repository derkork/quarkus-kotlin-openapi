package com.ancientlightstudios.quarkus.kotlin.openapi.inspection

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle

class RequestBundleInspection(val bundle: OpenApiRequestBundle) {

    fun requests(block: RequestInspection.() -> Unit) = bundle.requests.forEach { RequestInspection(it).block() }

}