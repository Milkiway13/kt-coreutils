import platform.posix.*
import kotlinx.cinterop.*

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: mv-kt <source> <destination>")
        return
    }

    val source = args[0]
    val destination = args[1]

    if (rename(source, destination) != 0) {
        perror("mv failed")
        platform.posix.exit(1)
    }
}
