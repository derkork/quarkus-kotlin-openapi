package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinRenderableBlockContainer<T : KotlinRenderable>(private val separateItemsWithNewLine: Boolean = true) {

    private val items = mutableListOf<T>()

    fun addItem(item: T) {
        items.add(item)
    }

    val isNotEmpty: Boolean
        get() = items.isNotEmpty()

    fun render(writer: CodeWriter) = with(writer) {
        items.forEachWithStats { status, item ->
            item.render(this)
            if (!status.last) {
                writeln(forceNewLine = false) // in case the item already rendered a line break
                if (separateItemsWithNewLine) {
                    writeln()
                }
            }
        }
    }

}