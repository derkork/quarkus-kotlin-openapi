package com.ancientlightstudios.quarkus.kotlin.openapi.emitter

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.AnnotationAware
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinAnnotation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName.Companion.rawClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName

fun AnnotationAware.addPathAnnotation(path: String) {
    addAnnotation(
        KotlinAnnotation(
            "Path".rawClassName(), "value".variableName() to path.stringExpression()
        )
    )
}
