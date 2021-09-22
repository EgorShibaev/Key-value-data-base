import java.io.FileNotFoundException
import java.util.regex.PatternSyntaxException

fun parseCommand(text: String?): List<String>? {
	if (text == null)
		return null
	val result = mutableListOf(text.split(' ').first())
	var lastQuotes: Char? = null
	var indexOfLastQuotes: Int = -1
	for (i in result[0].length until text.length)
		when {
			text[i] == lastQuotes -> {
				result.add(text.substring(indexOfLastQuotes + 1, i))
				lastQuotes = null
			}
			lastQuotes == null && text[i] in listOf('\'', '"') -> {
				lastQuotes = text[i]
				indexOfLastQuotes = i
			}
			lastQuotes == null && text[i] !in listOf('\'', '"', ' ') -> return null
		}
	return result
}

fun Map<String, String>.joinToString(): String {
	return map { "${it.key} -> ${it.value}" }.joinToString(separator = "\n")
}

fun processFindCommand(cont: Map<String, String>, command: List<String>) = when (command[0]) {
	"find" -> when {
		command.size != 2 -> println("Incorrect command")
		cont.containsKey(command[1]) -> println(cont[command[1]])
		else -> println("Database do not contain this key")
	}
	else -> when {
		command.size != 2 -> println("Incorrect command")
		else -> {
			try {
				println(cont.filter { it.key.matches(command[1].toRegex()) }.joinToString())
			} catch (e: PatternSyntaxException) {
				println("Error: ${e.message}")
			}
		}
	}
}

fun processEraseCommand(cont: MutableMap<String, String>, command: List<String>) = when (command[0]) {
	"erase" -> when {
		command.size != 2 -> println("Incorrect command")
		cont.containsKey(command[1]) -> {
			cont.remove(command[1])
			println("Done")
		}
		else -> println("Database do not contain this key")
	}
	else -> when {
		command.size != 2 -> println("Incorrect command")
		else -> {
			try {
				cont.minusAssign(cont.keys.filter { it.matches(command[1].toRegex()) })
				println("Done")
			} catch (e: PatternSyntaxException) {
				println("Error: ${e.message}")
			}
		}
	}
}

fun askKeyFromUser(): String {
	print("Key word:")
	return readLine()!!
}

fun greeting(): MutableMap<String, String> {
	println("Hello!!!")
	print("Do you want to continue work with database or start with empty database?[Continue/Start]")
	var answer = readLine()!!
	while (answer !in listOf("Continue", "Start")) {
		println("Choose from two option(Continue/Start)")
		answer = readLine()!!
	}
	return if (answer == "Continue") {
		readBase(askKeyFromUser()).toMutableMap()
	} else {
		writeToBase(mapOf(), "K")
		mutableMapOf()
	}
}

fun workingProcess(cont: MutableMap<String, String>): MutableMap<String, String> {
	var exit = false
	while (!exit) {
		print("write your command:")
		val command = parseCommand(readLine())
		if (command == null) {
			println("Incorrect command")
			continue
		}
		when (command[0]) {
			"find", "findRegex" -> processFindCommand(cont, command)
			"erase", "eraseRegex" -> processEraseCommand(cont, command)
			"set" -> when {
				command.size != 3 -> println("Incorrect command")
				else -> {
					cont[command[1]] = command[2]
					println("Done")
				}
			}
			"content" -> when {
				command.size != 1 -> println("Incorrect command")
				else -> println(cont.joinToString())
			}
			"clear" -> when {
				command.size != 1 -> println("Incorrect command")
				else -> {
					cont.clear()
					println("Done")
				}
			}
			"save" -> {
				writeToBase(cont, askKeyFromUser())
				println("Done")
			}
			"exit" -> exit = true
			else -> println("Incorrect command")
		}
	}
	return cont
}

fun main() {
	try {
		workingProcess(greeting())
	} catch (e: FileNotFoundException) {
		println(e.message)
	} catch (e: IndexOutOfBoundsException) {
		println("database is damaged. Do you want to clear database?[Y/N]")
		val answer = readLine()!!
		if (answer == "Y") {
			writeToBase(mapOf(), "K")
			println("Done")
		}
	}
}
