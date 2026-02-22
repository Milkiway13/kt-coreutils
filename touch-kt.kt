import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: touch <filename>")
        return
    }

    val path = args[0]
    val fd = open(path, O_CREAT or O_WRONLY, "644".toInt(8))

    if (fd == -1) {
        perror("touch: cannot touch '$path'")
        return
    }

    if (utimes(path, null) == -1) {
        perror("touch: setting times for '$path'")
    }

    close(fd)
}
