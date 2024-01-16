package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ClientRestInterfaceNameHint : Hint<ClassName> {

    var TransformableRequestBundle.clientRestInterfaceName: ClassName
        get() = get(ClientRestInterfaceNameHint) ?: ProbableBug("Name of the client rest interface not set")
        set(value) = set(ClientRestInterfaceNameHint, value)

}