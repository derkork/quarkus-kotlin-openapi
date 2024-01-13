package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.models.hints.Hint

open class TransformableObject {

    private val hints = mutableMapOf<Hint<*>, Any>()

    fun <T : Any> set(key: Hint<T>, value: T) {
        hints[key] = value
    }

    fun <T : Any> has(key: Hint<T>) = hints.containsKey(key)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: Hint<T>, default: (() -> T)? = null): T? =
        hints[key] as? T ?: default?.invoke()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrPut(key: Hint<T>, default: (() -> T)): T = hints.getOrPut(key, default) as T

    fun <T : Any> clear(key: Hint<T>) {
        hints.remove(key)
    }
}