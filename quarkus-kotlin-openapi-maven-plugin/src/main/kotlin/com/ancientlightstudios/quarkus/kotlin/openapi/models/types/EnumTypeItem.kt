package com.ancientlightstudios.quarkus.kotlin.openapi.models.types

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ConstantName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinExpression

class EnumTypeItem(val sourceName: String, val name: ConstantName, val value: KotlinExpression)