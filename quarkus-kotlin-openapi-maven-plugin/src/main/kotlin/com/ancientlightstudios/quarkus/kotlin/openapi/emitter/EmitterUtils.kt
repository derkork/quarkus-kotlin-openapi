package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.AnnotationAware
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinAnnotation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.Misc
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.literal
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.RequestMethod

fun AnnotationAware.addPathAnnotation(path: String) {
    addAnnotation(KotlinAnnotation(Misc.PathAnnotationClass, "value".variableName() to path.literal()))
}

fun AnnotationAware.addRequestMethodAnnotation(method: RequestMethod) {
    val className = when (method) {
        RequestMethod.Get -> Misc.GetAnnotationClass
        RequestMethod.Put -> Misc.PutAnnotationClass
        RequestMethod.Post -> Misc.PostAnnotationClass
        RequestMethod.Delete -> Misc.DeleteAnnotationClass
        RequestMethod.Options -> Misc.OptionsAnnotationClass
        RequestMethod.Head -> Misc.HeadAnnotationClass
        RequestMethod.Patch -> Misc.PatchAnnotationClass
        RequestMethod.Trace -> Misc.TraceAnnotationClass
    }
    addAnnotation(KotlinAnnotation(className))
}