package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ServerDelegateClassNameHint : Hint<ClassName> {

    var TransformableRequestBundle.serverDelegateClassName: ClassName
        get() = get(ServerDelegateClassNameHint) ?: ProbableBug("Name of the server delegate not set")
        set(value) = set(ServerDelegateClassNameHint, value)

}