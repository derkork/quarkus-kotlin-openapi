package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ServerRestInterfaceClassNameHint : Hint<ClassName> {

    var TransformableRequestBundle.serverRestInterfaceClassName: ClassName
        get() = get(ServerRestInterfaceClassNameHint) ?: ProbableBug("Name of the server rest interface not set")
        set(value) = set(ServerRestInterfaceClassNameHint, value)

}