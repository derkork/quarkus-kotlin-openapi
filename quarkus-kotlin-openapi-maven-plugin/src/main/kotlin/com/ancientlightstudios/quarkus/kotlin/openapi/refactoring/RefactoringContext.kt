package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.InterfaceType
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class RefactoringContext(val spec: TransformableSpec, val config: Config) {

    val interfaceType = config.interfaceType

    val interfacePackage = config.packageName

    val modelPackage = "${config.packageName}.model"

    val withTestSupport = config.interfaceType == InterfaceType.TEST_CLIENT

    fun performRefactoring(refactoring: SpecRefactoring) {
        refactoring.apply { perform() }
    }

}