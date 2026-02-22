import platform.posix.*

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: chmod <octal_mode> <file>")
        return
    }

    val mode = args[0].toUInt(8)
    val path = args[1]

    if (chmod(path, mode.toUInt()) == -1) {
        perror("chmod: changing permissions for '$path'")
    }
}
