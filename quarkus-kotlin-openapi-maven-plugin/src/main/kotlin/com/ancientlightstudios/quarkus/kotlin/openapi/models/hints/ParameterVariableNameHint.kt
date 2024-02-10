package com.ancientlightstudios.quarkus.kotlin.openapi.models.hints

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableBody
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableParameter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSchemaProperty
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.ProbableBug

// specifies the variable name of a request parameter, body or schema property
object ParameterVariableNameHint : Hint<VariableName> {

    var TransformableParameter.parameterVariableName: VariableName
        get() = get(ParameterVariableNameHint) ?: ProbableBug("Name of the parameter not set")
        set(value) = set(ParameterVariableNameHint, value)

    var TransformableBody.parameterVariableName: VariableName
        get() = get(ParameterVariableNameHint) ?: ProbableBug("Name of the parameter not set")
        set(value) = set(ParameterVariableNameHint, value)

    var TransformableSchemaProperty.parameterVariableName: VariableName
        get() = get(ParameterVariableNameHint) ?: ProbableBug("Name of the parameter not set")
        set(value) = set(ParameterVariableNameHint, value)

}