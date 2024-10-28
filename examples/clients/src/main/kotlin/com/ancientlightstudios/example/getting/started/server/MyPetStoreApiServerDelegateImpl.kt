package com.ancientlightstudios.example.getting.started.server

import com.ancientlightstudios.example.clients.client.FindPetsByStatusError
import com.ancientlightstudios.example.clients.client.FindPetsByStatusHttpResponse
import com.ancientlightstudios.example.clients.client.UpstreamPetStoreApiClient
import com.ancientlightstudios.example.clients.client.model.FindPetsByStatusStatusParameter
import com.ancientlightstudios.example.clients.server.GetAvailablePetsContext
import com.ancientlightstudios.example.clients.server.MyPetStoreApiServerDelegate
import com.ancientlightstudios.example.clients.server.model.GetAvailablePets500Response
import com.ancientlightstudios.example.clients.server.model.Pet
import com.ancientlightstudios.quarkus.kotlin.openapi.IsError
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MyPetStoreApiServerDelegateImpl(
    private val petStoreApiClient: UpstreamPetStoreApiClient
) : MyPetStoreApiServerDelegate {
    override suspend fun GetAvailablePetsContext.getAvailablePets(): Nothing {
        // Call the remote server
        val response = petStoreApiClient.findPetsByStatus(FindPetsByStatusStatusParameter.Available)
/*
        // error handling
        val message = when (response) {
            // Request was successful
            is FindPetsByStatusHttpResponse.Ok -> {
                // Return the pets.Calling ok will immediately stop execution here.
                ok(response.safeBody.map { Pet(it.id, it.name ?: "no name", it.photoUrls ?: listOf()) })
            }

            // We sent a bad request to the remote server. This is usually a bug in our code.
            is FindPetsByStatusHttpResponse.BadRequest -> "We sent a bad request to the remote server. This is usually a bug in our code."

            // We have various possible error cases which can occur when calling the remote server.
            // How we handle these depends on the specific requirements of our application. In our case
            // we'll just extract an error message that we return to our own caller. We could however react
            // to each error case differently (e.g. with different responses). Since our API only has a single
            // simple error response type we'll just return that.
            is FindPetsByStatusError.RequestErrorConnectionReset -> "Connection was reset when calling the remote server."
            is FindPetsByStatusError.RequestErrorTimeout -> "Timeout when calling the remote server."
            is FindPetsByStatusError.RequestErrorUnreachable -> "The remote server could not be reached."
            is FindPetsByStatusError.RequestErrorUnknown -> {
                Log.warn("An unknown error occurred when calling the remote server.", response.cause)
                "An unknown error occurred when calling the remote server (usually indicates misconfiguration, SSL certificates being wrong, etc.)."
            }
            is FindPetsByStatusError.ResponseError -> "The response from the remote server did not adhere to the OpenAPI spec. Reason: ${response.reason}"
        }

        // Return the error message
        internalServerError(GetAvailablePets500Response(message))
        */

        if (response is FindPetsByStatusHttpResponse.Ok) {
            ok(response.safeBody.map { Pet(it.id, it.name ?: "no name", it.photoUrls ?: listOf()) })
        }

        // Return the error message
        internalServerError(GetAvailablePets500Response("Internal error."))

    }

}