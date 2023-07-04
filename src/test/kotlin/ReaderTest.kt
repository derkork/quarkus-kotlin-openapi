import com.tallence.quarkus.kotlin.openapi.Config
import com.tallence.quarkus.kotlin.openapi.Generator
import com.tallence.quarkus.kotlin.openapi.builder.parseAsApiSpec
import com.tallence.quarkus.kotlin.openapi.read
import org.junit.jupiter.api.Test

class ReaderTest {

    @Test
    fun generateWorks() {
        val config = Config(
            listOf(javaClass.getResource("/example_openapi.json")!!.path),
            "MyInterface",
            "com.tallence.quarkus.kotlin.openapi",
            "target/generated-sources/kotlin",
            listOf("/openapi/subscription/{SearchTerm}"),
        )

        val generator = Generator(config)
        generator.generate()
    }
}