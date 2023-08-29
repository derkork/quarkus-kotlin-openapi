package com.ancientlightstudios.example;

import com.ancientlightstudios.example.model.*
import com.ancientlightstudios.example.server.NarfInterface
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Path
import java.util.UUID


@Path("/")
class NarfServer(val narfInterface:NarfInterface) : NarfInterface {


    // lf4j logger
    private val logger = org.slf4j.LoggerFactory.getLogger(NarfServer::class.java)
    override suspend fun addPet(body: PetUnsafe?): Pet {
        TODO("Not yet implemented")
    }

    override suspend fun updatePet(body: PetUnsafe?): Pet {
        TODO("Not yet implemented")
    }

    override suspend fun findPetsByStatus(status: FindPetsByStatus_statusUnsafe?): List<Any> {
        logger.info("findPetsByStatus: $status")
        return listOf()
    }

    override suspend fun findPetsByTags(tags: List<Any>?): List<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun getPetById(petId: Int?): Pet {
        TODO("Not yet implemented")
    }

    override suspend fun updatePetWithForm(petId: Int?, name: String?, status: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePet(api_key: String?, petId: Int?) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadFile(petId: Int?, additionalMetadata: String?): ApiResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getInventory(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun placeOrder(body: OrderUnsafe?): Order {
        TODO("Not yet implemented")
    }

    override suspend fun getOrderById(orderId: Int?): Order {
        TODO("Not yet implemented")
    }

    override suspend fun deleteOrder(orderId: Int?) {
        TODO("Not yet implemented")
    }

    override suspend fun createUser(body: UserUnsafe?) {
        TODO("Not yet implemented")
    }

    override suspend fun createUsersWithListInput(body: List<Any>?): User {
        TODO("Not yet implemented")
    }

    override suspend fun loginUser(username: String?, password: String?): String {
        TODO("Not yet implemented")
    }

    override suspend fun logoutUser() {
        TODO("Not yet implemented")
    }

    override suspend fun getUserByName(username: String?): User {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(username: String?, body: UserUnsafe?) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(username: String?) {
        TODO("Not yet implemented")
    }


}
