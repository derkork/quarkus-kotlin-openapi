package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

// specifies a name suggestion
object NameSuggestionHint : Hint<String> {

    // available for request parameter, request body, response (not the response body), response header and schemas
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