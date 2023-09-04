package com.ancientlightstudios.quarkus.kotlin.openapi.hort

class KotlinFile(val packageName: String, val imports: List<String>) {

    lateinit var content: KotlinFileContent

    fun write() {
        // create writer for class name
        // render package

        // render imports
        // delegate to content
    }

}
