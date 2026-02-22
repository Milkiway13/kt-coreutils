import platform.posix.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: rm-kt [-r] <file or directory>")
        return
    }

    val recursive = args.contains("-r")
    val targets = args.filter { it != "-r" }

    for (target in targets) {
        removePath(target, recursive)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun removePath(path: String, recursive: Boolean) {
    memScoped {
        val statBuf = alloc<stat>()
        if (stat(path, statBuf.ptr) != 0) {
            perror("rm: $path")
            return
        }

        val isDirectory = (statBuf.st_mode.toInt() and S_IFMT) == S_IFDIR

        if (isDirectory && recursive) {
            val dir = opendir(path) ?: return
            try {
                while (true) {
                    val entry = readdir(dir) ?: break
                    val name = entry.pointed.d_name.toKString()
                    if (name == "." || name == "..") continue
                    removePath("$path/$name", true)
                }
            } finally {
                closedir(dir)
            }
            if (rmdir(path) != 0) perror("rmdir $path")
        } else if (isDirectory && !recursive) {
            println("rm: cannot remove '$path': Is a directory")
        } else {
            if (unlink(path) != 0) perror("unlink $path")
        }
    }
}
