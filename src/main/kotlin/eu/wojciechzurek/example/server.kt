package eu.wojciechzurek.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import java.nio.file.Path

const val CONNECTION_POOL = 16

sealed interface Message

data class Connection(val sc: SocketChannel) : Message
object Terminate : Message //not used in this example

fun main() {
    runBlocking {
        val channel = Channel<Message>()
        val jobs = mutableListOf<Job>() //required for terminating jobs, not used in this example

        repeat(CONNECTION_POOL) { id ->
            val job = launch(Dispatchers.IO) {
                println("[$id] Worker created")
                while (isActive) when (val message = channel.receive()) {
                    is Connection -> message.sc.use { handler(id, it)() }
                    is Terminate -> break
                }
                println("[$id] Worker terminated")
            }
            jobs.add(job)
        }

        try {
            val path = System.getenv("SOCKET_PATH") ?: System.getProperties().getProperty("user.home")
            val socketPath = Path.of(path).resolve("server.socket")
            Files.deleteIfExists(socketPath)

            val address = UnixDomainSocketAddress.of(socketPath)
            ServerSocketChannel.open(StandardProtocolFamily.UNIX).bind(address).use {
                println("Waiting for client to connect ${it.localAddress}")

                while (true) {
                    val sc = it.accept()
                    println("Connection accepted")
                    channel.send(Connection(sc))
                }
            }


        } catch (e: Exception) {
            println("Error: ${e.message}")
            jobs.forEach {
                it.cancelAndJoin()
            }
        }
    }
}

fun handler(id: Int, sc: SocketChannel): () -> Unit = {
    println("[$id] Client connected")
    try {
        sc.write(ByteBuffer.wrap("[$id]  Hello from echo server!\n".encodeToByteArray()))

        while (sc.isConnected) when (val m = read(sc)) {
            is Ok -> {
                if (m.data == "quit\n") {
                    sc.write(ByteBuffer.wrap("[$id] Bye!\n".encodeToByteArray()))
                    println("[$id] Client quit")
                    break
                }
                print("[$id] Incoming message: ${m.data}")
                sc.write(ByteBuffer.wrap("[$id]  Echo: ${m.data}".encodeToByteArray()))
            }
            Error -> break
        }
    } catch (e: Exception) {
        println("[$id] Error ${e.message}")
    }
}
