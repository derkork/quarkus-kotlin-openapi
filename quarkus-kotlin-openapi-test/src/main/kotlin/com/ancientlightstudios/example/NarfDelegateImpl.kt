package com.ancientlightstudios.example

import com.ancientlightstudios.example.model.GetUserRequest
import com.ancientlightstudios.example.model.User
import com.ancientlightstudios.example.server.NarfInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.Logger


@ApplicationScoped
class NarfDelegateImpl : NarfInterface {

    private val log: Logger = org.slf4j.LoggerFactory.getLogger(NarfDelegateImpl::class.java)

    override suspend fun getUser(request: Maybe<GetUserRequest>): User = request.unwrap {
        log.info("requesting user ${it.userId} with status filter ${it.status}")
        User(it.userId, "Host", it.status)
    }
}

fun <T, S> Maybe<T>.unwrap(block: (T) -> S): S {
    when (this) {
        is Maybe.Success -> {
            return block(this.value)
        }

        is Maybe.Failure -> throw IllegalArgumentException("Delegate! " + this.errors.joinToString { it.message })
    }
}