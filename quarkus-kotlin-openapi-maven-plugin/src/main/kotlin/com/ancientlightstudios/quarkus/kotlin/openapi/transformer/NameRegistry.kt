package com.ancientlightstudios.quarkus.kotlin.openapi.transformer

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

class NameRegistry {

    private val nameBuilder = mutableMapOf<ClassName, NameBuilder>()

    fun uniqueNameFor(name: ClassName): ClassName {
        val builder = nameBuilder[name]
        return if (builder != null) {
            builder.next()
        } else {
            nameBuilder[name] = NameBuilder(name)
            name
        }
    }

    private class NameBuilder(private val name: ClassName) {

        private var nextIndex = 1

        fun next() = name.extend(postfix = "${nextIndex++}")

    }

}
