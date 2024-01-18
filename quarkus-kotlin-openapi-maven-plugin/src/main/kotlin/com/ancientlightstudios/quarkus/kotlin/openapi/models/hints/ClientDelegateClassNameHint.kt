package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ClientDelegateClassNameHint : Hint<ClassName> {

    var TransformableRequestBundle.clientDelegateClassName: ClassName
        get() = get(ClientDelegateClassNameHint) ?: ProbableBug("Name of the client delegate not set")
        set(value) = set(ClientDelegateClassNameHint, value)

}