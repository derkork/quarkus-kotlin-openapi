package com.ancientlightstudios.quarkus.kotlin.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.EmitterStage
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable.TransformableSpec
import com.ancientlightstudios.quarkus.kotlin.openapi.parser.ParserStage
import com.ancientlightstudios.quarkus.kotlin.openapi.patching.PatchingStage
import com.ancientlightstudios.quarkus.kotlin.openapi.refactoring.RefactoringStage
import com.ancientlightstudios.quarkus.kotlin.openapi.validation.ValidationStage
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

class Generator(private val config: Config) {

    private val log = LoggerFactory.getLogger(Generator::class.java)
    
    fun generate() {
        val start = Instant.now()

        val json = PatchingStage(config).process()
        val spec = TransformableSpec()
        ParserStage(config, json).process(spec)
        ValidationStage().process(spec)
        RefactoringStage(config).process(spec)
        // TODO: needs another validation stage. e.g. check that the enum-default value is a valid item
        EmitterStage(config).process(spec)

        val duration = Duration.between(start, Instant.now())

        log.info("Plugin took ${duration.prettyPrint()}")
    }

    private fun Duration.prettyPrint() : String {
        val hours = toHoursPart()
        val minutes = toMinutesPart()
        val seconds = toSecondsPart()
        val milliseconds = toMillisPart()

        val result = StringBuilder()
        if (hours > 0) {
            result.append("${hours}h")
        }
        if (minutes > 0) {
            result.append("${minutes}m")
        }
        if (seconds > 0) {
            result.append("${seconds}s")
        }
        // ignore milliseconds if there are minutes or hours
        if (hours == 0 && minutes == 0 && milliseconds > 0) {
            result.append("${milliseconds}ms")
        }
        
        return result.toString()
    }
}
