package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

abstract class MaybeTransformStatement : KotlinStatement {
    fun CodeWriter.renderValidation(validationInfo: ValidationInfo) {
        if (validationInfo.required) {
            writeln(".required()")
        }
    }
}