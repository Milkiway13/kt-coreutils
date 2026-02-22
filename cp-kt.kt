import platform.posix.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: cp-kt <source> <destination>")
        return
    }

    val source = args[0]
    var destination = args[1]

    memScoped {
        val statBuf = alloc<stat>()
        if (stat(destination, statBuf.ptr) == 0) {
            val isDirectory = (statBuf.st_mode.toInt() and S_IFMT) == S_IFDIR
            if (isDirectory) {
                val filename = source.split("/").last()
                destination = if (destination.endsWith("/")) "$destination$filename" else "$destination/$filename"
            }
        }
    }

    // 2. Perform the copy
    val srcFile = fopen(source, "rb")
    if (srcFile == null) {
        perror("cp: cannot open source '$source'")
        return
    }

    val destFile = fopen(destination, "wb")
    if (destFile == null) {
        fclose(srcFile)
        perror("cp: cannot create destination '$destination'")
        return
    }

    try {
        val buffer = ByteArray(65536)
        buffer.usePinned { pinned ->
            while (true) {
                val bytesRead = fread(pinned.addressOf(0), 1UL, buffer.size.toULong(), srcFile)
                if (bytesRead <= 0U) break
                fwrite(pinned.addressOf(0), 1UL, bytesRead, destFile)
            }
        }
    } finally {
        fclose(srcFile)
        fclose(destFile)
    }
}
