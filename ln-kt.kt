import platform.posix.*

fun main(args: Array<String>) {
    val isSymbolic = args.contains("-s")
    val filteredArgs = args.filter { it != "-s" }

    if (filteredArgs.size < 2) {
        println("Usage: ln-kt [-s] <target> <link_name>")
        return
    }

    val target = filteredArgs[0]
    val linkName = filteredArgs[1]

    val result = if (isSymbolic) {
        symlink(target, linkName)
    } else {
        link(target, linkName)
    }

    if (result == -1) {
        perror("ln-kt: failed to create link")
    }
}
