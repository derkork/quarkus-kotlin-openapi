package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.OpenApiParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the variable name of a request parameter, body or schema property
object ParameterVariableNameHint : Hint<VariableName> {

    var OpenApiParameter.parameterVariableName: VariableName
        get() = get(ParameterVariableNameHint) ?: ProbableBug("Name of the parameter not set")
        set(value) = set(ParameterVariableNameHint, value)

    var OpenApiBody.parameterVariableName: VariableName
        get() = get(ParameterVariableNameHint) ?: ProbableBug("Name of the parameter not set")
        set(value) = set(ParameterVariableNameHint, value)

}