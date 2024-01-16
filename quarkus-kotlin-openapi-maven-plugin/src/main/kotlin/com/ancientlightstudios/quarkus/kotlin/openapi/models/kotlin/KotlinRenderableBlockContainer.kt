package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinRenderableBlockContainer<T : KotlinRenderable>(private val separateItemsWithEmptyLine: Boolean = true) :
    KotlinRenderable {

    private val items = mutableListOf<T>()

    fun addItem(item: T) {
        items.add(item)
    }

    val isNotEmpty: Boolean
        get() = items.isNotEmpty()

    override fun ImportCollector.registerImports() {
        registerFrom(items)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        items.forEachWithStats { status, item ->
            item.render(this)
            if (!status.last) {
                writeln(forceNewLine = false) // in case the item already rendered a line break
                if (separateItemsWithEmptyLine) {
                    writeln()
                }
            }
        }
    }

}