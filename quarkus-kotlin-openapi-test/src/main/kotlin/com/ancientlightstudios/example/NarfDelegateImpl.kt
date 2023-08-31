package com.ancientlightstudios.example

import com.ancientlightstudios.example.model.*
import com.ancientlightstudios.example.server.NarfInterface
import com.ancientlightstudios.quarkus.kotlin.openapi.Maybe
import jakarta.enterprise.context.ApplicationScoped
import org.slf4j.Logger


@ApplicationScoped
class NarfDelegateImpl : NarfInterface {

    private val log: Logger = org.slf4j.LoggerFactory.getLogger(NarfDelegateImpl::class.java)

    override suspend fun notifyUser(request: Maybe<NotifyUserRequest>) = request.unwrap {
        log.info("notifying users ${it.body.size}")
    }

    override suspend fun getUsers(request: Maybe<GetUsersRequest>): List<User> = request.unwrap {
        log.info("requesting users with status filter ${it.status}")
        listOf(User(1, "arnie", UserStatus.Available),
            User(1, "arnie", UserStatus.Available))
    }

    override suspend fun getUser(request: Maybe<GetUserRequest>): User = request.unwrap {
        log.info("requesting user ${it.userId} with status filter ${it.status}")
        User(it.userId, it.arnie, it.status ?: UserStatus.Available)
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