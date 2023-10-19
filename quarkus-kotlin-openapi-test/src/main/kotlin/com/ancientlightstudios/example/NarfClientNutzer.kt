package com.ancientlightstudios.example

import com.ancientlightstudios.example.client.FindMoviesResponse
import com.ancientlightstudios.example.client.NarfInterfaceClient
import com.ancientlightstudios.quarkus.kotlin.openapi.RequestResult
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.Response

@ApplicationScoped
class NarfClientNutzer(private val client: NarfInterfaceClient) {

    suspend fun machWas(): Int {
        val result = client.findMovies(null, null, null) as? RequestResult.Response
            ?: throw IllegalStateException("Server was bad")

        return when (val resultResponse = result.response) {
            is FindMoviesResponse.Ok -> resultResponse.safeBody.size
            else -> errorHandler(resultResponse.status, resultResponse.unsafeBody).run { -12 }
        }
    }

    private fun errorHandler(statusCode: Response.Status, unsafeBody: Any?) {

    }

}