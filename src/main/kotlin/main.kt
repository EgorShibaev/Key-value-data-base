import java.io.FileNotFoundException
import java.util.regex.PatternSyntaxException

enum class Command {
	CONTENT, INSERT, UPDATE, FIND, FINDREGEX, ERASE, ERASEREGEX, EXIT, SAVE, CLEAR, ROLLBACK
}

data class Operation(val erased: List<Pair<String, String>>, val inserted: List<String>)

fun rollBackOperation(cont: MutableMap<String, String>, operation: Operation) {
	operation.inserted.forEach {
		cont.remove(it)
	}
	operation.erased.forEach {
		cont[it.first] = it.second
	}
}

/**
 * This function processes command.
 * Firstly, first word is found and determined the command.
 * Then arguments of this command are processed.
 * If command need two arguments, "->" is found two string which are split by "->" is arguments
 * If command need one argument string after ' ' is argument
 * */
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
		"rollback" -> Command.ROLLBACK
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
		Command.CLEAR, Command.SAVE, Command.EXIT, Command.CONTENT, Command.ROLLBACK -> {
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


/**
 * This function has three arguments:
 * cont - content of database
 * command - command (erase or eraseRegex) and arguments of this command
 * operations - list of operation in which program will add current operation
 * */
fun processEraseCommand(
	cont: MutableMap<String, String>,
	command: Pair<Command, List<String>>,
	operations: MutableList<Operation>
) = when (command.first) {
	Command.ERASE -> when {
		cont.containsKey(command.second[0]) -> {
			operations.add(Operation(listOf(Pair(command.second[0], cont.getValue(command.second[0]))), listOf()))
			cont.remove(command.second[0])
			println("Done")
		}
		else -> println("Database do not contain this key")
	}
	else -> {
		try {
			val fieldsForRemove = cont.keys.filter { it.matches(command.second[0].toRegex()) }
			val listOfRemoved = fieldsForRemove.map {
				Pair(it, cont.getValue(it))
			}
			operations.add(Operation(listOfRemoved, listOf()))
			cont.minusAssign(fieldsForRemove)
			println("This field is removed\n$fieldsForRemove")
		} catch (e: PatternSyntaxException) {
			println("Error: ${e.message}")
		}
	}
}

// arguments are the same as in processEraseCommand
fun processChangeCommand(
	cont: MutableMap<String, String>,
	command: Pair<Command, List<String>>,
	operations: MutableList<Operation>
) = when (command.first) {
	Command.INSERT -> {
		if (cont.containsKey(command.second[0]))
			println("Database contains this key")
		else {
			cont[command.second[0]] = command.second[1]
			operations.add(Operation(listOf(), listOf(command.second[0])))
			println("Done")
		}
	}
	else -> {
		if (!cont.containsKey(command.second[0]))
			println("Database does not contain this key")
		else {
			operations.add(
				Operation(
					listOf(Pair(command.second[0], cont.getValue(command.second[0]))),
					listOf(command.second[0])
				)
			)
			cont[command.second[0]] = command.second[1]
			println("Done")
		}
	}
}

fun askKeyFromUser(): String {
	print("Key word:")
	return readLine()!!
}

/**
 * This function ask user what he wants: continue work with database or start with empty database.
 * If he wants to continue, program ask key for decipher database
 * The function return content of database
 * */
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
		// key does not matter
		writeToBase(mapOf(), "K")
		mutableMapOf()
	}
}

fun sizeOfOperation(operation: Operation) = operation.erased.sumOf { it.first.length + it.second.length } +
		operation.inserted.sumOf { it.length }

/**
 * This function organizes working process
 * argument is content of database.
 * there is the loop while in which program read command and process it.
 * There is maxSizeOfOperations and when size of operations is more than it, the latest operations from list is removed.
 * */
fun workingProcess(cont: MutableMap<String, String>) {
	var exit = false
	val maxSizeOfOperations = 100
	val operations = mutableListOf<Operation>()
	while (!exit) {
		print("write your command:")
		val command = parseCommand(readLine())
		if (command == null) {
			println("Incorrect command")
			continue
		}
		when (command.first) {
			Command.FIND, Command.FINDREGEX -> processFindCommand(cont, command)
			Command.ERASE, Command.ERASEREGEX -> processEraseCommand(cont, command, operations)
			Command.UPDATE, Command.INSERT -> processChangeCommand(cont, command, operations)
			Command.CONTENT -> println(cont.joinToString())
			Command.CLEAR -> {
				operations.add(Operation(cont.map { Pair(it.key, it.value) }, listOf()))
				cont.clear()
				println("Done")
			}
			Command.ROLLBACK -> {
				if (operations.isNotEmpty()) {
					rollBackOperation(cont, operations.last())
					operations.removeLast()
					println("Done")
				} else
					println("Last operation is not saved (or not exist)")
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
		while (operations.sumOf{ sizeOfOperation(it) } > maxSizeOfOperations)
			operations.removeFirst()
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
