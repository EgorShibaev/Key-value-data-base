import java.io.File

fun readBase2(): Map<String, String> {
	val lines = File("database").readLines()
	val result = mutableMapOf<String, String>()
	for (i in lines.indices step 2)
		result[lines[i]] = lines[i + 1]
	return result
}

fun writeToBase2(lines: Map<String, String>) {
	File("database").writeText(lines.flatMap { listOf(it.key, it.value) }.joinToString(separator = "\n"))
}