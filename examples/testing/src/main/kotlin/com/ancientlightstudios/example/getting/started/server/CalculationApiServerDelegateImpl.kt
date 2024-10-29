package com.ancientlightstudios.example.getting.started.server

import com.ancientlightstudios.example.testing.server.CalculationApiServerDelegate
import com.ancientlightstudios.example.testing.server.SumContext
import com.ancientlightstudios.example.testing.server.model.ErrorInfo
import com.ancientlightstudios.example.testing.server.model.Result
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class CalculationApiServerDelegateImpl : CalculationApiServerDelegate {
    override suspend fun SumContext.sum(): Nothing {
        // Verify the request
        val validRequestBody = request.validOrElse { errors ->
            // return an error response if the request is invalid
            badRequest(ErrorInfo(message = errors.joinToString("\n") { "${it.path} : ${it.message}" }))
        }.body

        ok(Result(result = validRequestBody.a + validRequestBody.b))
    }

}