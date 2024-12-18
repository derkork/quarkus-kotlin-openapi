package com.ancientlightstudios.example.features.server

import com.ancientlightstudios.quarkus.kotlin.openapi.validOrElse
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class FeaturesValidationServerDelegateImpl : FeaturesValidationServerDelegate {

    override suspend fun OptionalValueContext.optionalValue(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun NullableValueContext.nullableValue(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun BigDecimalValidationContext.bigDecimalValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun BigIntegerValidationContext.bigIntegerValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun NumberValidationContext.numberValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }
    
    override suspend fun StringLengthValidationContext.stringLengthValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun StringPatternValidationContext.stringPatternValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun ArrayValidationContext.arrayValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun ConstraintsValidationContext.constraintsValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun ResponseValidationContext.responseValidation(): Nothing {
        val validRequest = request.validOrElse { badRequest(it.toOperationError()) }
        ok(validRequest.response)
    }

    override suspend fun PropertiesOnPureMapValidationContext.propertiesOnPureMapValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun PropertiesOnNestedMapValidationContext.propertiesOnNestedMapValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun PropertiesOnObjectValidationContext.propertiesOnObjectValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }

    override suspend fun PropertiesOnObjectWithDefaultValidationContext.propertiesOnObjectWithDefaultValidation(): Nothing {
        request.validOrElse { badRequest(it.toOperationError()) }
        noContent()
    }
}
