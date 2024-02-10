package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableRequestBundle
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the class name of the server delegate (the interface implemented by the application)
object ServerDelegateClassNameHint : Hint<ClassName> {

    var TransformableRequestBundle.serverDelegateClassName: ClassName
        get() = get(ServerDelegateClassNameHint) ?: ProbableBug("Name of the server delegate not set")
        set(value) = set(ServerDelegateClassNameHint, value)

}