package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.example.features.server.model.*
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesParameterServerDelegateImpl : FeaturesParametersServerDelegate {

    override suspend fun ParametersPathContext.parametersPath(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(ParametersPath200Response(validRequest.name, validRequest.id))
    }

    override suspend fun ParametersRequiredNotNullContext.parametersRequiredNotNull(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(
            ParametersRequiredNotNull200Response(
                validRequest.querySingleValue,
                validRequest.queryCollectionValue,
                validRequest.headerSingleValue,
                validRequest.headerCollectionValue,
                validRequest.cookieSingleValue
            ),
            validRequest.headerSingleValue,
            validRequest.headerCollectionValue
        )
    }

    override suspend fun ParametersRequiredNullableContext.parametersRequiredNullable(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(
            ParametersRequiredNullable200Response(
                validRequest.querySingleValue,
                validRequest.queryCollectionValue,
                validRequest.headerSingleValue,
                validRequest.headerCollectionValue,
                validRequest.cookieSingleValue
            ),
            validRequest.headerSingleValue,
            validRequest.headerCollectionValue
        )
    }

    override suspend fun ParametersOptionalNotNullContext.parametersOptionalNotNull(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(
            ParametersOptionalNotNull200Response(
                validRequest.querySingleValue,
                validRequest.queryCollectionValue,
                validRequest.headerSingleValue,
                validRequest.headerCollectionValue,
                validRequest.cookieSingleValue
            ),
            validRequest.headerSingleValue,
            validRequest.headerCollectionValue
        )
    }

    override suspend fun ParametersOptionalNullableContext.parametersOptionalNullable(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }

        ok(
            ParametersOptionalNullable200Response(
                validRequest.querySingleValue,
                validRequest.queryCollectionValue,
                validRequest.headerSingleValue,
                validRequest.headerCollectionValue,
                validRequest.cookieSingleValue
            ),
            validRequest.headerSingleValue,
            validRequest.headerCollectionValue
        )
    }
}