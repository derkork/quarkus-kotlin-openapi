package com.ancientlightstudios.quarkus.kotlin.openapi

import org.apache.maven.plugins.annotations.Parameter

class ComplexSource {
    @Parameter(required = true)
    lateinit var sources:List<String>
}