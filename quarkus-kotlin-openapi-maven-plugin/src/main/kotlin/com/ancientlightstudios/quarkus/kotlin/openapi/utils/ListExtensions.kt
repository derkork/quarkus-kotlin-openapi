package com.ancientlightstudios.quarkus.kotlin.openapi.utils

fun <T> List<T>.replaceWith(old: T, vararg new: T): List<T> {
    val index = this.indexOf(old)
    if (index == -1) {
        return this
    }

    val itemsBefore = this.take(index)
    val itemsAfter = this.drop(index + 1)
    return itemsBefore + new + itemsAfter
}
