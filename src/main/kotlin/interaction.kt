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

fun reverseSort(key : String): List<Int> {
	val sort = key.withIndex().sortedBy { it.value }
	val reverseSort = MutableList(key.length) { 0 }
	sort.withIndex().forEach {
		reverseSort[it.value.index] = it.index
	}
	return reverseSort
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
	// reverse sort
	val reverseSortOrder = reverseSort(key)
	val sortedTable = (0 until rows).map { table[reverseSortOrder[it]] }
	val result = StringBuilder()
	for (row in 0 until rows)
		for (column in 1 until columns)
			result.append(sortedTable[row][column])
	while (result.last() == 0.toChar())
		result.delete(result.length - 1, result.length)
	return result.toString()
}

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