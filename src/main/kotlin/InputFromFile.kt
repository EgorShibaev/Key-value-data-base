import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.lang.IllegalArgumentException

fun processArguments(args: Array<String>): Pair<String, String?>? {
	if (args.isEmpty())
		return null
	if (args[0] != "-f" && args[0] != "--file")
		throw IllegalArgumentException("${args[0]} is not permissible argument")
	if (args.size != 2 && args.size != 3)
		throw IllegalArgumentException("-f option must one or two arguments")
	return Pair(args[1], args.getOrNull(2))
}

fun workWithFile(nameInput: String, nameOutput: String?) {
	val standardOut = System.out
	val standardIn = System.`in`
	val inputFile = File(nameInput)
	val stream = ByteArrayOutputStream()
	System.setOut(PrintStream(stream))
	System.setIn(ByteArrayInputStream(inputFile.readBytes()))
	workingProcess(greeting())
	if (nameOutput != null) {
		File(nameOutput).writeBytes(
			cleanOutOutput(stream.toString()).toByteArray()
		)
	}
	System.setOut(standardOut)
	System.setIn(standardIn)
}

fun cleanOutOutput(text: String): String {
	val listOfRubbish = listOf(
		"Key word:",
		"Hello!!!\nDo you want to continue work with database or start with empty database?[Continue/Start]",
		"Wrong key. Do you want to clear database?[Y/N]\n",
		"Choose from two option(Continue/Start):\n",
		"write your command:",
		"Do you want to save data?[Y/N]\n"
	)
	var result = text
	listOfRubbish.forEach { result = result.replace(it, "") }
	return result
}