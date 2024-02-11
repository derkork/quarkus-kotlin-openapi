package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinStatementContainer : KotlinRenderable {

    private val items = mutableListOf<KotlinStatement>()

    fun addItem(item: KotlinStatement) {
        items.add(item)
    }

    val isEmpty: Boolean
        get() = items.isEmpty()

    val isNotEmpty: Boolean
        get() = items.isNotEmpty()

    val size: Int
        get() = items.size

    override fun ImportCollector.registerImports() {
        registerFrom(items)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        items.forEachWithStats { status, item ->
            indent(skipFirstLine = true) {
                item.render(this)
                if (!status.last) {
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }
            }
        }
    }

}