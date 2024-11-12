package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiResponse

// specifies the class name of a response
object ResponseInterfaceNameHint : Hint<ClassName> {

    var OpenApiResponse.responseInterfaceName: ClassName?
        get() = get(ResponseInterfaceNameHint)
        set(value) {
            if (value == null) {
                clear(ResponseInterfaceNameHint)
            } else {
                set(ResponseInterfaceNameHint, value)
            }
        }
}