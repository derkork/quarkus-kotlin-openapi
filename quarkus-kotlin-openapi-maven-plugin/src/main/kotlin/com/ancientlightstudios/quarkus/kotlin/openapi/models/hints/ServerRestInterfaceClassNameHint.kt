package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the name of the server class (the class which does the actual request handling)
object ServerRestInterfaceClassNameHint : Hint<ClassName> {

    var OpenApiRequestBundle.serverRestInterfaceClassName: ClassName
        get() = get(ServerRestInterfaceClassNameHint) ?: ProbableBug("Name of the server rest interface not set")
        set(value) = set(ServerRestInterfaceClassNameHint, value)

}