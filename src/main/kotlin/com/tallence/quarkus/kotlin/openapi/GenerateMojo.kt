package com.tallence.quarkus.kotlin.openapi

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

class GenerateMojo :  AbstractMojo() {
    /**
     * The current Maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    lateinit var project: MavenProject

    /**
     * The list of the source files.
     */
    @Parameter
    lateinit var sources: List<String>

    /**
     * The package name of the generated classes.
     */
    @Parameter
    lateinit var packageName: String


    /**
     * The directory where the generated sources should be put
     */
    @Parameter(defaultValue = "\${project.build.directory}/generated-sources/quarkus-kotlin-openapi")
    lateinit var outputDirectory: File

    override fun execute() {
        TODO("Not yet implemented")
    }

}