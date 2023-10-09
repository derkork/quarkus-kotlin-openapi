package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.RequestFilter
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.merge
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.parseAsApiSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.read
import java.io.File

class Generator(private val config: Config) {

    fun generate() = parse()
        .transform()
        .write()

    private fun parse() = config.sourceFiles
        .map { read(File(it).inputStream()) }
        .reduce { acc, apiSpec -> acc.merge(apiSpec) }
        .parseAsApiSpec(RequestFilter(config.endpoints))

    private fun ApiSpec.transform(): ApiSpec {
//        val files = transform(apiSpec, config)
    }

    private fun ApiSpec.write() {
//        write(files, config)
    }

}