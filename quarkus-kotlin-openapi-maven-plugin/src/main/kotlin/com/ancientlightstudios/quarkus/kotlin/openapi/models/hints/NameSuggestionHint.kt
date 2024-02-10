package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject

// specifies a name suggestion
object NameSuggestionHint : Hint<String> {

    var TransformableObject.nameSuggestion: String?
        get() = get(NameSuggestionHint)
        set(value) {
            if (value == null) {
                clear(NameSuggestionHint)
            } else {
                set(NameSuggestionHint, value)
            }
        }

}