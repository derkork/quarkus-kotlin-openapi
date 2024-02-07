package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType

class ModelClassEmitter(private val interfaceType: InterfaceType) : CodeEmitter {

    override fun EmitterContext.emit() {
        EnumModelClassEmitter(interfaceType).apply { emit() }
        DefaultObjectModelClassEmitter(interfaceType).apply { emit() }
        AnyOfObjectModelClassEmitter(interfaceType).apply { emit() }
        OneOfObjectModelClassEmitter(interfaceType).apply { emit() }
    }

}
