package com.ancientlightstudios.example.getting.started.server

import com.ancientlightstudios.example.getting.started.server.model.GetHelloWorld200Response
import com.ancientlightstudios.example.getting.started.server.model.GetHelloWorld400Response
import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class HelloWorldApiServerDelegateImpl : HelloWorldApiServerDelegate {

    override suspend fun GetHelloWorldContext.getHelloWorld(): Nothing {
        // verify that the request is valid, if not return a bad request with some error message.
        val validRequest = request.validOrElse { errors ->
            // the errors are a list of validation errors. We join them to a single string for the response.
            // as our API only provides for a single error message.
            val errorMessage = errors.joinToString(",") {
                // the "path" contains the path to the invalid field, the "message" contains the error message.
                it.path + ": " + it.message
            }
            badRequest(GetHelloWorld400Response(errorMessage))
        }

        // generate the response, in this case we just return a hello world message including the "who"
        // parameter from the request.
        ok(GetHelloWorld200Response("Hello ${validRequest.who}!"))
    }
}