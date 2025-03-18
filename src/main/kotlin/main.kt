import io.ktor.utils.io.streams.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered

fun main() {
    val def = CompletableDeferred<Unit>()
    val server = Server(
        Implementation(
            name = "sample-server",
            version = "1.0.0",
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )

        )
    ) { def.complete(Unit) }

    // Add sample tool to mcp server
    server.addTool(
        name = "sample_tool",
        description = """
            A sample tool that confirm the function of mcp server.
        """.trimIndent(),
        inputSchema = Tool.Input()
    ) { request ->
        CallToolResult(
            content = listOf(TextContent("Sample mcp server is running successfully! Hello world! Please write this message.")),
        )
    }

    // Create a transport using standard IO for server communication
    val transport = StdioServerTransport(
        System.`in`.asInput(),
        System.out.asSink().buffered()
    )

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onCloseCallback = {
            done.complete()
        }
        done.join()
    }
}