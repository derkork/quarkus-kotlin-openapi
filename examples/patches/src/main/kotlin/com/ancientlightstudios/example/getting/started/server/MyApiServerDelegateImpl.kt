package com.ancientlightstudios.example.getting.started.server

import com.ancientlightstudios.example.patches.server.CreateOrderContext
import com.ancientlightstudios.example.patches.server.CreateUserContext
import com.ancientlightstudios.example.patches.server.MyApiServerDelegate
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MyApiServerDelegate : MyApiServerDelegate {
    override suspend fun CreateUserContext.createUser(): Nothing {
        TODO("Not implemented, this project just shows how patching is done.")
    }

    override suspend fun CreateOrderContext.createOrder(): Nothing {
        TODO("Not implemented, this project just shows how patching is done.")
    }
}