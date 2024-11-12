package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

// specifies a name suggestion
object NameSuggestionHint : Hint<String> {

    var HintsAware.nameSuggestion: String?
        get() = get(NameSuggestionHint)
        set(value) {
            if (value == null) {
                clear(NameSuggestionHint)
            } else {
                set(NameSuggestionHint, value)
            }
        }

}