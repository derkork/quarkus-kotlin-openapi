package com.ancientlightstudios.example

import com.ancientlightstudios.example.model.CreateUserRequest
import com.ancientlightstudios.example.server.NarfInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.Logger


@ApplicationScoped
class NarfDelegateImpl : NarfInterface {

    private val log: Logger = org.slf4j.LoggerFactory.getLogger(NarfDelegateImpl::class.java)
    override suspend fun createUser(request: Maybe<CreateUserRequest>) {
        if (request is Maybe.Failure) {
            throw IllegalArgumentException("Delegate! " + request.errors.joinToString { it.message })
        }

        log.info("Total geile scheisse")

    }
}