import java.io.File

/**
 * This function and function decipher is used for encryption. Here is used encryption by permutation:
 * https://ru.wikipedia.org/wiki/%D0%9F%D0%B5%D1%80%D0%B5%D1%81%D1%82%D0%B0%D0%BD%D0%BE%D0%B2%D0%BE%D1%87%D0%BD%D1%8B%D0%B9_%D1%88%D0%B8%D1%84%D1%80
 * Text is written in table in rows and then rows is sorted based on the key. Then text is read by columns.
 * Some text is added to end with goal check in decipher is text ends with this ending.
 * */
fun encrypt(text: String, key: String): String {
	val textWithCodeword = text + "secret word"
	val rows = key.length
	// calculate count of columns the table should have
	val columns = (textWithCodeword.length + rows - 1) / rows + 1
	// first char of each row is letter of key
	val table = key.map { (listOf(it) + List(columns - 1) { 0.toChar() }).toMutableList() }
	var currRow = 0
	var currCol = 1
	// fulfill the table with text
	textWithCodeword.forEach {
		table[currRow][currCol] = it
		currCol++
		if (currCol == columns) {
			// newline
			currCol = 1
			currRow++
		}
	}
	val sortedTable = table.sortedBy { it.first() }
	val result = StringBuilder()
	// read table by columns
	for (column in 1 until columns)
		for (row in 0 until rows)
			result.append(sortedTable[row][column])
	return result.toString()
}

/**
 * This function find permutation such that composition this permutation and permutation which sort key is
 * identical permutation.
 * */
fun reverseSortPermutation(key: String): List<Int> {
	val permutation = key.withIndex().sortedBy { it.value }
	val result = MutableList(key.length) { 0 }
	permutation.withIndex().forEach {
		result[it.value.index] = it.index
	}
	return result
}


/**
 * This function is used for decipher text. Firstly, table is fulfilled in columns.
 * Next step is roll back sort. Program find reverse permutation and permute rows of table.
 * Then table is read by rows and extra char on the end is deleted. Then program check
 * that word at the end equals to secret word which was added in the function encrypt.
 * */
fun decipher(text: String, key: String): String {
	// length of text must be divided by length of key
	if (key.isEmpty() || text.length % key.length != 0)
		throw IllegalAccessError()
	val rows = key.length
	val columns = text.length / rows + 1
	val table = key.map { (listOf(it) + List(columns - 1) { 0.toChar() }).toMutableList() }
	var currRow = 0
	var currCol = 1
	// fulfill the table in columns
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
	// read text by rows
	for (row in 0 until rows)
		for (column in 1 until columns)
			result.append(initialTable[row][column])
	// remove extra chars
	while (result.isNotEmpty() && result.last() == 0.toChar())
		result.delete(result.length - 1, result.length)
	// check if string at the end equals string program added in encrypt
	if (result.substring(result.length - "secret word".length) != "secret word")
		throw IllegalAccessError()
	// remove this string from the end
	return result.removeSuffix("secret word").toString()
}

/**
 * This two function below are responsible for interaction with database.
 * readBase receive key and call decipher with this key.
 * All lines are split into groups of two. In each group first line
 * is key and second is value.
 * */
fun readBase(key: String): Database {
	val text = decipher(File("database").readText(), key)
	val lines = if (text.isNotEmpty()) text.split('\n') else listOf()
	val result = Database(mutableMapOf(), mutableMapOf())
	val countOfGroups = lines[0].toInt()
	var currentLine = 1
	repeat(countOfGroups) {
		val nameOfGroup = lines[currentLine]
		val countOfKeys = lines[currentLine + 1].toInt()
		currentLine += 2
		val keys = mutableListOf<String>()
		repeat(countOfKeys) {
			keys.add(lines[currentLine])
			currentLine++
		}
		result.groups[nameOfGroup] = keys
	}
	while (currentLine < lines.size) {
		result.content[lines[currentLine]] = lines[currentLine + 1]
		currentLine += 2
	}
	return result
}

fun writeToBase(database: Database, key: String) {
	val result = StringBuilder()
	result.append("${database.groups.size}\n")
	database.groups.forEach {
		result.append("${it.key}\n")
		result.append("${it.value.size}\n")
		it.value.forEach { key ->
			result.append("${key}\n")
		}
	}
	database.content.forEach {
		result.append("${it.key}\n")
		result.append("${it.value}\n")
	}
	File("database").writeText(encrypt(result.removeSuffix("\n").toString(), key))
}