package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.RequestMethod

class RequestFilter(endpoints: List<String>) {

    private val endpoints = endpoints.flatMap {
        if (!it.contains(":")) {
            // only a path, without methods
            listOf(it)
        } else {
            // at least one method available
            val (path, methods) = it.split(":", limit = 2)
            methods.split(",")
                .map { method -> RequestMethod.fromString(method) }
                .map { method -> toFilterValue(path, method) }
        }
    }.toSet()

    fun accept(path: String, method: RequestMethod): Boolean {
        // no filter specified, so all endpoints are valid
        if (endpoints.isEmpty()) {
            return true
        }

        // the endpoint itself is specified, so all methods are valid
        if (endpoints.contains(path)) {
            return true
        }
        
        return endpoints.contains(toFilterValue(path, method))
    }

    private fun toFilterValue(path: String, method: RequestMethod) = "$path:${method.name}"

}
