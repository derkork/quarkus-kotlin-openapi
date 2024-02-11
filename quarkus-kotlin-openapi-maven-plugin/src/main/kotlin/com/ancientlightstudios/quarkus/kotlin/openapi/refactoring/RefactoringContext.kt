package com.ancientlightstudios.quarkus.kotlin.openapi.refactoring

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec

class RefactoringContext(val spec: TransformableSpec, private val config: Config) {

    val interfacePackage = config.packageName

    val modelPackage = "${config.packageName}.model"

    val apiPackage = "com.ancientlightstudios.quarkus.kotlin.openapi"

    fun performRefactoring(refactoring: SpecRefactoring) {
        refactoring.apply { perform() }
    }

}