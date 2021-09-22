import java.io.FileNotFoundException
import java.util.regex.PatternSyntaxException

enum class Command {
	CONTENT, SET, FIND, FINDREGEX, ERASE, ERASEREGEX, EXIT, SAVE, CLEAR
}

fun parseCommand(text: String?): Pair<Command, List<String>>? {
	if (text == null)
		return null
	val command = when (text.split(' ').first()) {
		"content" -> Command.CONTENT
		"set" -> Command.SET
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
		Command.SET -> {
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
		Command.FIND, Command.FINDREGEX, Command.ERASE, Command.ERASEREGEX -> {
			if (!text.contains(' '))
				null
			else
				Pair(command, listOf(text.substring(text.indexOf(' ') until text.length).trim()))
		}
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
			Command.SET -> {
				cont[command.second[0]] = command.second[1]
				println("Done")
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
			Command.EXIT -> exit = true
		}
	}
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
