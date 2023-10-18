package com.ancientlightstudios.quarkus.kotlin.openapi.utils

data class LoopStatus(val index: Int, val first: Boolean, val last: Boolean)

inline fun <T> Collection<T>.forEachWithStats(action: (status: LoopStatus, T) -> Unit) {
    val lastIndex = size - 1
    this.forEachIndexed { index, item -> action(LoopStatus(index, index == 0, index == lastIndex), item) }
}

inline fun <T> Array<T>.forEachWithStats(action: (status: LoopStatus, T) -> Unit) {
    val lastIndex = size - 1
    this.forEachIndexed { index, item -> action(LoopStatus(index, index == 0, index == lastIndex), item) }
}
