import java.io.FileNotFoundException

enum class Command {
	CONTENT, INSERT, UPDATE, FIND, FINDREGEX, ERASE, ERASEREGEX, EXIT, SAVE, CLEAR, ROLLBACK
}

data class Operation(val erased: List<Pair<String, String>>, val inserted: List<String>) {
	fun getSize() = erased.sumOf { it.first.length + it.second.length } +
			inserted.sumOf { it.length }
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
	val result = map { "${it.key} -> ${it.value}" }.sorted().joinToString(separator = "\n")
	return if (result == "") "Base is empty" else result
}

fun askKeyFromUser(): String {
	print("Key word:")
	var answer : String
	do {
		answer = readLine() ?: throw IllegalArgumentException("Key hasn't been read")
		if (answer.isEmpty())
			println("key cannot be empty. Try again.")
	} while (answer.isEmpty())
	return answer
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
		var keyIsCorrect = false
		var res = mutableMapOf<String, String>()
		while (!keyIsCorrect) {
			try {
				res = readBase(askKeyFromUser()).toMutableMap()
				keyIsCorrect = true
			} catch (e: IllegalAccessError) {
				println("Wrong key.")
			}
		}
		res
	} else {
		// key does not matter
		writeToBase(mapOf(), "K")
		mutableMapOf()
	}
}

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
			Command.CLEAR -> processClearCommand(cont, operations)
			Command.ROLLBACK -> processRollBackCommand(cont, operations)
			Command.SAVE -> processSaveCommand(cont)
			Command.EXIT -> {
				exit = true
				processExitCommand(cont)
			}
		}
		while (operations.sumOf{ it.getSize() } > maxSizeOfOperations)
			operations.removeFirst()
	}
}

fun main(args : Array<String>) {
	try {
		val options = processArguments(args)
		if (options != null)
			workWithFile(options.first, options.second)
		else
			workingProcess(greeting())
	} catch (e: FileNotFoundException) {
		println(e.message)
	} catch (e : IllegalArgumentException) {
		println(e.message)
	}
}
