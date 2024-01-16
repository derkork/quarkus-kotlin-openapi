package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ServerRestInterfaceNameHint : Hint<ClassName> {

    var TransformableRequestBundle.serverRestInterfaceName: ClassName
        get() = get(ServerRestInterfaceNameHint) ?: ProbableBug("Name of the server rest interface not set")
        set(value) = set(ServerRestInterfaceNameHint, value)

}