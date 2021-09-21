import java.io.File

fun encrypt(text: String, key: String): String {
	val rows = key.length
	val columns = (text.length + rows - 1) / rows + 1
	val table = key.map { (listOf(it) + List(columns - 1) { 0.toChar() }).toMutableList() }
	var currRow = 0
	var currCol = 1
	text.forEach {
		table[currRow][currCol] = it
		currCol++
		if (currCol == columns) {
			currCol = 1
			currRow++
		}
	}
	val sortedTable = table.sortedBy { it.first() }
	val result = StringBuilder()
	for (column in 1 until columns)
		for (row in 0 until rows)
			result.append(sortedTable[row][column])
	return result.toString()
}

fun reverseSortPermutation(key: String): List<Int> {
	val permutation = key.withIndex().sortedBy { it.value }
	val result = MutableList(key.length) { 0 }
	permutation.withIndex().forEach {
		result[it.value.index] = it.index
	}
	return result
}

fun decipher(text: String, key: String): String {
	val rows = key.length
	val columns = text.length / rows + 1
	val table = key.map { (listOf(it) + List(columns - 1) { 0.toChar() }).toMutableList() }
	var currRow = 0
	var currCol = 1
	text.forEach {
		table[currRow][currCol] = it
		currRow++
		if (currRow == rows) {
			currRow = 0
			currCol++
		}
	}
	// roll back sort
	val reverseSortOrder = reverseSortPermutation(key)
	val initialTable = (0 until rows).map { table[reverseSortOrder[it]] }
	val result = StringBuilder()
	for (row in 0 until rows)
		for (column in 1 until columns)
			result.append(initialTable[row][column])
	while (result.isNotEmpty() && result.last() == 0.toChar())
		result.delete(result.length - 1, result.length)
	return result.toString()
}

fun readBase(key: String): Map<String, String> {
	val lines = decipher(File("database").readText(), key).split('\n')
	val result = mutableMapOf<String, String>()
	for (i in lines.indices step 2)
		result[lines[i]] = lines[i + 1]
	return result
}

fun writeToBase(lines: Map<String, String>, key : String) {
	val text = encrypt(lines.flatMap { listOf(it.key, it.value) }.joinToString(separator = "\n"), key)
	File("database").writeText(text)
}