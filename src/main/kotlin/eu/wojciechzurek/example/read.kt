package eu.wojciechzurek.example

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

sealed interface Result
data class Ok(val data: String) : Result
object Error : Result

const val BUFFER_CAPACITY = 128

fun read(sc: SocketChannel): Result {
    ByteBuffer.allocate(BUFFER_CAPACITY).let {
        val read = sc.read(it)
        return if (read > 0) {
            val ba = ByteArray(read)
            it.flip()
            it.get(ba)
            Ok(String(ba))
        } else {
            Error
        }
    }
}