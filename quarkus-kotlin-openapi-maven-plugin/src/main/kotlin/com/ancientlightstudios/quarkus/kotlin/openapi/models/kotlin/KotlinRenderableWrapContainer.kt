package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.renderWithWrap

class KotlinRenderableWrapContainer<T : KotlinRenderable>(val maxSizeForSingleLine : Int = 1) {

    private val items = mutableListOf<T>()

    fun addItem(item: T) {
        items.add(item)
    }

    val isNotEmpty: Boolean
        get() = items.isNotEmpty()

    fun render(writer: CodeWriter) = with(writer) {
        renderWithWrap(items, maxSizeForSingleLine = maxSizeForSingleLine) { it.render(this) }
    }

}