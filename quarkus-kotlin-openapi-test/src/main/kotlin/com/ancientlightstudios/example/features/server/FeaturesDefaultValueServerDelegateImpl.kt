package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.DefaultValueResponseObject
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesDefaultValueServerDelegateImpl : FeaturesDefaultValueServerDelegate {

    override suspend fun DefaultParameterValuesContext.defaultParameterValues(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(
            DefaultValueResponseObject(
                validRequest.stringParam,
                validRequest.booleanParam,
                validRequest.intParam,
                validRequest.uintParam,
                validRequest.longParam,
                validRequest.ulongParam,
                validRequest.floatParam,
                validRequest.doubleParam,
                validRequest.bigDecimalParam,
                validRequest.bigIntegerParam
            )
        )
    }

    override suspend fun DefaultBodyValuesContext.defaultBodyValues(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        val body = validRequest.body

        ok(
            DefaultValueResponseObject(
                body.stringParam,
                body.booleanParam,
                body.intParam,
                body.uintParam,
                body.longParam,
                body.ulongParam,
                body.floatParam,
                body.doubleParam,
                body.bigDecimalParam,
                body.bigIntegerParam
            )
        )
    }
}