package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ClientRestInterfaceClassNameHint : Hint<ClassName> {

    var TransformableRequestBundle.clientRestInterfaceClassName: ClassName
        get() = get(ClientRestInterfaceClassNameHint) ?: ProbableBug("Name of the client rest interface not set")
        set(value) = set(ClientRestInterfaceClassNameHint, value)

}