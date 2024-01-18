package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

object ParameterVariableNameHint : Hint<VariableName> {

    var TransformableParameter.parameterVariableName: VariableName
        get() = get(ParameterVariableNameHint) ?: ProbableBug("Name of the parameter not set")
        set(value) = set(ParameterVariableNameHint, value)

}