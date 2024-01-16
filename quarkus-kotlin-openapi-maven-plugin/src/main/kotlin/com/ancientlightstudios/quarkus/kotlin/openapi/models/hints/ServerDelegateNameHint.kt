package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableObject
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ServerDelegateNameHint : Hint<ClassName> {

    var TransformableRequestBundle.serverDelegateName: ClassName
        get() = get(ServerDelegateNameHint) ?: ProbableBug("Name of the server delegate not set")
        set(value) = set(ServerDelegateNameHint, value)

}