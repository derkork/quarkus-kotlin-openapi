package com.ancientlightstudios.example

import com.ancientlightstudios.example.model.*
import com.ancientlightstudios.example.server.NarfInterfaceDelegate
import com.ancientlightstudios.quarkus.kotlin.openapi.*
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class NarfDelegateImpl : NarfInterfaceDelegate {
    override suspend fun notifyUser3(): NotifyUser3response {
        TODO("Not yet implemented")
    }

    override suspend fun notifyUser(request: Maybe<NotifyUserRequest>): NotifyUserResponse {
        val valid = request.validOrElse { return NotifyUserResponse.status(400, it) }
        return NotifyUserResponse.status(200, "die scheisse geht jetzt wirklich: ${valid.body?.size}")
    }

    override suspend fun notifyUser2(request: Maybe<NotifyUser2request>): NotifyUser2response {
        TODO("Not yet implemented")
    }

    override suspend fun getUsers(request: Maybe<GetUsersRequest>): GetUsersResponse {
        val validRequest = request.validOrElse { return GetUsersResponse.status(400, it) }
        return GetUsersResponse.status(200, "die scheisse geht jetzt wirklich: ${validRequest.status} , ${validRequest.arnie}")
    }


    override suspend fun getUser(request: Maybe<GetUserRequest>): GetUserResponse {
        TODO("Not yet implemented")
    }
}