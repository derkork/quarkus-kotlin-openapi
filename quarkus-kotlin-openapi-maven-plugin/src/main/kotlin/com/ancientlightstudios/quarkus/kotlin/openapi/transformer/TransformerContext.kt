import com.ancientlightstudios.quarkus.kotlin.openapi.Config
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinFile
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.Request
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.SchemaRef
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.*

class TransformerContext(val config: Config) {
    private val done = mutableSetOf<QueueItem>()
    private val queue = mutableSetOf<QueueItem>()
    private val safeModels = mutableMapOf<SchemaRef, SafeModelQueueItem>()
    private val unsafeModels = mutableMapOf<SchemaRef, UnsafeModelQueueItem>()

    fun safeModelFor(schemaRef: SchemaRef): SafeModelQueueItem {
        return safeModels.getOrPut(schemaRef) { SafeModelQueueItem(schemaRef, this) }.apply { queue.add(this) }
    }

    fun unsafeModelFor(schemaRef: SchemaRef): UnsafeModelQueueItem {
        return unsafeModels.getOrPut(schemaRef) { UnsafeModelQueueItem(schemaRef, this) }.apply { queue.add(this) }
    }

    fun requestContainerFor(request: Request): RequestContainerQueueItem? {
        return if (request.parameters.isEmpty() && request.body == null) {
            null
        } else {
            RequestContainerQueueItem(request, this).apply { queue.add(this) }
        }
    }


    fun enqueue(item: QueueItem): QueueItem {
        queue.add(item)
        return item
    }

    fun run(): List<KotlinFile> {
        val result = mutableListOf<KotlinFile>()

        while (queue.isNotEmpty()) {
            val item = queue.first()
            queue.remove(item)

            if (!done.contains(item)) {
                done.add(item)
                val element = item.generate()
                if (element != null) {
                    result.add(element)
                }
            }
        }

        return result
    }
}