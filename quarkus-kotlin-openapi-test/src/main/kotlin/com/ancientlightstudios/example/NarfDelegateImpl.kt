package com.ancientlightstudios.example

import com.ancientlightstudios.example.model.*
import com.ancientlightstudios.example.model.ValidationError
import com.ancientlightstudios.example.server.NarfInterfaceDelegate
import com.ancientlightstudios.quarkus.kotlin.openapi.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.RestResponse
import java.lang.IllegalArgumentException
import java.util.UUID
import kotlin.random.Random

@ApplicationScoped
class NarfDelegateImpl : NarfInterfaceDelegate {

    private val users = mutableListOf<User>()

    override suspend fun find(request: Maybe<FindRequest>): FindResponse {
        val validRequest = request.validOrElse { return FindResponse.badRequest(it.asResponseBody()) }

        val filteredUsers = validRequest.status?.let { status -> users.filter { it.status == status } } ?: users
        return FindResponse.ok(filteredUsers)
    }

    override suspend fun add(request: Maybe<AddRequest>): AddResponse {
        val validRequest = request.validOrElse { return AddResponse.badRequest(it.asResponseBody()) }

        val body = validRequest.body
        val newUser =
            User(generateId(), body.name, body.age, body.status, body.address, body.tags, body.fallbackAddresses)
        users.add(newUser)
        return AddResponse.ok(newUser)
    }

    override suspend fun get(request: Maybe<GetRequest>): GetResponse {
        val validRequest = request.validOrElse { return GetResponse.badRequest(it.asResponseBody()) }
        val user = findUserOrElse(validRequest.userId) { return GetResponse.notFound(it) }
        return GetResponse.ok(user)
    }

    override suspend fun modify(request: Maybe<ModifyRequest>): ModifyResponse {
        val validRequest = request.validOrElse { return ModifyResponse.badRequest(it.asResponseBody()) }
        val user = findUserOrElse(validRequest.userId) { return ModifyResponse.notFound(it) }

        val body = validRequest.body
        val newUser = User(user.id, body.name, body.age, body.status, body.address, body.tags, body.fallbackAddresses)
        users.replaceAll { if (it.id == newUser.id) newUser else it }
        return ModifyResponse.ok(newUser)
    }

    override suspend fun delete(request: Maybe<DeleteRequest>): DeleteResponse {
        val validRequest = request.validOrElse { return DeleteResponse.badRequest(it.asResponseBody()) }
        val user = findUserOrElse(validRequest.userId) { return DeleteResponse.notFound(it) }

        users.remove(user)
        return DeleteResponse.noContent()
    }

    private inline fun findUserOrElse(userId: Id, block: (ApplicationError) -> Nothing) =
        users.firstOrNull { it.id == userId } ?: block(ApplicationError("user with id $userId not found"))

}

fun generateId() = Id(UUID.randomUUID().toString())

fun List<com.ancientlightstudios.quarkus.kotlin.openapi.ValidationError>.asResponseBody() =
    ValidationError(this.map { "'${it.path}': ${it.message}" })
