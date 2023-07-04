import com.tallence.quarkus.kotlin.openapi.builder.parseAsApiSpec
import com.tallence.quarkus.kotlin.openapi.read
import org.junit.jupiter.api.Test

class ReaderTest {

    @Test
    fun readWorks() {
        val inputStream = javaClass.getResourceAsStream("/example_openapi.json")!!
        val jsonNode = read(inputStream).parseAsApiSpec()
        println(jsonNode)
    }
}