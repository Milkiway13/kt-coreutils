import kotlinx.cinterop.toKString
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.getlogin

@OptIn(ExperimentalForeignApi::class)
fun main() {
    val userPtr = getlogin()
    val user = userPtr?.toKString() ?: "unknown"
    println(user)
}
