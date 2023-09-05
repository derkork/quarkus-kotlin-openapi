package com.ancientlightstudios.quarkus.kotlin.openapi.writer

import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import java.io.File

fun write(files: List<KotlinFile>, config: Config) {

    for (kotlinFile in files) {
        val file =
            File("${config.outputDirectory}/${kotlinFile.packageName.replace(".", "/")}/${kotlinFile.fileName}.kt")
        file.parentFile.mkdirs()
        check(file.exists() || file.createNewFile()) { "Could not create file $file" }

        file.bufferedWriter().use {
            kotlinFile.render(CodeWriter(it))
        }
    }

}