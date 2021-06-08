package eu.wojciechzurek.example

import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.file.Path
import java.util.*


fun main() {
    val socketPath = Path.of(System.getProperties().getProperty("user.home")).resolve("server.socket")
    val address = UnixDomainSocketAddress.of(socketPath)

    SocketChannel.open(StandardProtocolFamily.UNIX).use { sc ->
        sc.connect(address)

        when (val m = read(sc)) {
            is Ok -> print(m.data)
            Error -> return
        }

        val scanner = Scanner(System.`in`)
        while (sc.isConnected) {
            val message = scanner.nextLine()

            val bb = ByteBuffer.wrap("$message\n".encodeToByteArray())

            try {
                sc.write(bb)
                when (val m = read(sc)) {
                    is Ok -> print(m.data)
                    Error -> break
                }
            } catch (e: Exception) {
                println(e.message)
                break
            }
        }
    }
}