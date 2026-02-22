import kotlinx.cinterop.*
import platform.posix.*

const val RESET = "\u001b[0m"
const val DEFAULT_DIR_COLOR = 4 

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val flags = args.filter { it.startsWith("-") }.flatMap { it.drop(1).toList() }
    val path = args.firstOrNull { !it.startsWith("-") } ?: "."
    val showHidden = 'a' in flags

    val dirColor = loadConfigColor()

    val dir = opendir(path)
    if (dir == null) {
        perror("ls-kt")
        return
    }

    try {
        val directories = mutableListOf<String>()
        val files = mutableListOf<String>()

        while (true) {
            val entry = readdir(dir) ?: break
            val name = entry.pointed.d_name.toKString()
            if (showHidden || !name.startsWith(".")) {
                val fullPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
                if (isDirectory(fullPath)) {
                    directories.add(name)
                } else {
                    files.add(name)
                }
            }
        }

        directories.sort()
        files.sort()

        directories.forEach { name ->
            print("\u001b[38;5;${dirColor}m$name$RESET  ")
        }
        files.forEach { name ->
            print("$name  ")
        }
        println()

    } finally {
        closedir(dir)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun isDirectory(path: String): Boolean {
    memScoped {
        val statBuf = alloc<stat>()
        if (stat(path, statBuf.ptr) == 0) {
            return (statBuf.st_mode.toInt() and S_IFMT) == S_IFDIR
        }
    }
    return false
}

@OptIn(ExperimentalForeignApi::class)
fun loadConfigColor(): Int {
    val home = getenv("HOME")?.toKString() ?: return DEFAULT_DIR_COLOR
    val configPath = "$home/.config/kt-coreutils/config.toml"
    val file = fopen(configPath, "r") ?: return DEFAULT_DIR_COLOR
    try {
        memScoped {
            val buffer = allocArray<ByteVar>(1024)
            while (fgets(buffer, 1024, file) != null) {
                val line = buffer.toKString().trim()
                if (line.startsWith("ls_dir_colour")) {
                    return line.substringAfter("=").trim().toIntOrNull() ?: DEFAULT_DIR_COLOR
                }
            }
        }
    } finally {
        fclose(file)
    }
    return DEFAULT_DIR_COLOR
}
