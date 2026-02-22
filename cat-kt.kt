import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: cat-kt <filename>")
        return
    }

    val fileName = args[0]
    val file = fopen(fileName, "r")

    if (file == null) {
        perror("cat-kt: $fileName")
        return
    }

    try {
        memScoped {
            val bufferSize = 1024
            val buffer = allocArray<ByteVar>(bufferSize)

            while (fgets(buffer, bufferSize, file) != null) {
                print(buffer.toKString())
            }
        }
    } finally {
        fclose(file)
    }
}
