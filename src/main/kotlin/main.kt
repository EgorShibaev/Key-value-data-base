import java.io.FileNotFoundException
import java.util.regex.PatternSyntaxException

enum class Command {
	CONTENT, INSERT, UPDATE, FIND, FINDREGEX, ERASE, ERASEREGEX, EXIT, SAVE, CLEAR
}

fun parseCommand(text: String?): Pair<Command, List<String>>? {
	if (text == null)
		return null
	val command = when (text.split(' ').first()) {
		"content" -> Command.CONTENT
		"insert" -> Command.INSERT
		"update" -> Command.UPDATE
		"find" -> Command.FIND
		"findRegex" -> Command.FINDREGEX
		"erase" -> Command.ERASE
		"eraseRegex" -> Command.ERASEREGEX
		"exit" -> Command.EXIT
		"save" -> Command.SAVE
		"clear" -> Command.CLEAR
		else -> return null
	}
	return when (command) {
		// these commands need two arguments separated by "->"
		Command.INSERT, Command.UPDATE -> {
			if (text.indexOf("->") == -1 || !text.contains(' '))
				null
			else
				Pair(
					command,
					listOf(
						text.substring(text.indexOf(' ') until text.indexOf("->")).trim(),
						text.substring(text.indexOf("->") + 2 until text.length).trim()
					)
				)
		}
		// these commands need one argument
		Command.FIND, Command.FINDREGEX, Command.ERASE, Command.ERASEREGEX -> {
			if (!text.contains(' '))
				null
			else
				Pair(command, listOf(text.substring(text.indexOf(' ') until text.length).trim()))
		}
		// these commands do not need arguments
		Command.CLEAR, Command.SAVE, Command.EXIT, Command.CONTENT -> {
			if (text.contains(' '))
				null
			else
				Pair(command, listOf())
		}
	}
}

fun Map<String, String>.joinToString(): String {
	val result = map { "${it.key} -> ${it.value}" }.joinToString(separator = "\n")
	return if (result == "") "Base is empty" else result
}

fun processFindCommand(cont: Map<String, String>, command: Pair<Command, List<String>>) = when (command.first) {
	Command.FIND -> when {
		cont.containsKey(command.second[0]) -> println(cont[command.second[0]])
		else -> println("Database do not contain this key")
	}
	else -> try {
		println(cont.filter { it.key.matches(command.second[0].toRegex()) }.joinToString())
	} catch (e: PatternSyntaxException) {
		println("Error: ${e.message}")
	}
}

fun processEraseCommand(cont: MutableMap<String, String>, command: Pair<Command, List<String>>) = when (command.first) {
	Command.ERASE -> when {
		cont.containsKey(command.second[0]) -> {
			cont.remove(command.second[0])
			println("Done")
		}
		else -> println("Database do not contain this key")
	}
	else -> {
		try {
			val fieldsForRemove = cont.keys.filter { it.matches(command.second[0].toRegex()) }
			cont.minusAssign(fieldsForRemove)
			println("This field is removed\n$fieldsForRemove")
		} catch (e: PatternSyntaxException) {
			println("Error: ${e.message}")
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
	while (answer.lowercase() !in listOf("continue", "start")) {
		println("Choose from two option(Continue/Start):")
		answer = readLine()!!
	}
	return if (answer.lowercase() == "continue") {
		readBase(askKeyFromUser()).toMutableMap()
	} else {
		writeToBase(mapOf(), "K")
		mutableMapOf()
	}
}

fun workingProcess(cont: MutableMap<String, String>) {
	var exit = false
	while (!exit) {
		print("write your command:")
		val command = parseCommand(readLine())
		if (command == null) {
			println("Incorrect command")
			continue
		}
		when (command.first) {
			Command.FIND, Command.FINDREGEX -> processFindCommand(cont, command)
			Command.ERASE, Command.ERASEREGEX -> processEraseCommand(cont, command)
			Command.INSERT -> {
				if (cont.containsKey(command.second[0]))
					println("Database contains this key")
				else {
					cont[command.second[0]] = command.second[1]
					println("Done")
				}
			}
			Command.UPDATE -> {
				if (!cont.containsKey(command.second[0]))
					println("Database does not contain this key")
				else {
					cont[command.second[0]] = command.second[1]
					println("Done")
				}
			}
			Command.CONTENT -> println(cont.joinToString())
			Command.CLEAR -> {
				cont.clear()
				println("Done")
			}
			Command.SAVE -> {
				writeToBase(cont, askKeyFromUser())
				println("Done")
			}
			Command.EXIT -> {
				exit = true
				println("Do you want to save data?[Y/N]")
				val answer = readLine()
				if (answer != "N" && answer != "n" && answer != null) {
					writeToBase(cont, askKeyFromUser())
					println("Data has been saved.")
				}
			}
		}
	}
}

fun main() {
	try {
		workingProcess(greeting())
	} catch (e: FileNotFoundException) {
		println(e.message)
	} catch (e: IllegalAccessError) {
		println("Wrong key. Do you want to clear database?[Y/N]")
		val answer = readLine()!!
		if (answer == "Y") {
			writeToBase(mapOf(), "K")
			println("Done")
		}
	}
}
