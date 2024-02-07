package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.components

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

interface SchemaDefinitionComponent {

    companion object {

        fun <T: SchemaDefinitionComponent> List<T>.singleOrNone(): T? {
            if (isEmpty()) {
                return null
            }

            if (size == 1) {
                return first()
            }

            ProbableBug("Component defined multiple times. Not yet supported.")
        }

         fun <T : SchemaDefinitionComponent> List<T>.baseMerge(block: (List<T>) -> T): T? {
            if (isEmpty()) {
                return null
            }

            if (size == 1) {
                return first()
            }

            return block(this)
        }

         fun <T: SchemaDefinitionComponent, R> List<T>.extract(block: (T) -> R?): R? {
            if (isEmpty()) {
                return null
            }

             val extracted = map(block).filterNot { it == null }
             return when(extracted.size) {
                 0 -> null
                 1 -> extracted.first()
                 else -> ProbableBug("Property defined multiple times. Not yet supported.")
             }
        }

    }

}