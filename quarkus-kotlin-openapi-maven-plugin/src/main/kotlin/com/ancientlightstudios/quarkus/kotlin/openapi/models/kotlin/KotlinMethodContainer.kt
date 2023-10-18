package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinMethodContainer {

    private val methods = mutableListOf<KotlinMethod>()

    fun addMethod(method: KotlinMethod) {
        methods.add(method)
    }

    val isNotEmpty: Boolean
        get() = methods.isNotEmpty()

    fun render(writer: CodeWriter) = with(writer) {
        methods.forEachWithStats { status, item ->
            item.render(this)
            if (!status.last) {
                writeln(forceNewLine = false) // in case the item already rendered a line break
                writeln()
            }
        }
    }
}