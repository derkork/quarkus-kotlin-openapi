package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ClientDelegateNameHint : Hint<ClassName> {

    var TransformableRequestBundle.clientDelegateName: ClassName
        get() = get(ClientDelegateNameHint) ?: ProbableBug("Name of the client delegate not set")
        set(value) = set(ClientDelegateNameHint, value)

}