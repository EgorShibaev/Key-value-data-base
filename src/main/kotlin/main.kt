import java.io.FileNotFoundException

enum class Command {
	CONTENT, INSERT, UPDATE, FIND, FIND_REGEX, ERASE, ERASE_REGEX, EXIT, SAVE, CLEAR, CREATE_GROUP,
	ERASE_GROUP, ERASE_FROM_GROUP, INSERT_IN_GROUP, FIND_IN_GROUP, CONTENT_OF_GROUP, CONTENT_OF_ALL_GROUPS, ROLLBACK
}

data class Database(val content: MutableMap<String, String>, val groups: MutableMap<String, MutableList<String>>)

/**
 * This function processes command.
 * Firstly, first word is found and determined the command.
 * If command of user is not found program find all possibilities.
 * Then arguments of this command are processed.
 * If command need two arguments, "->" is found two string which are split by "->" is arguments
 * If command need one argument string after ' ' is argument
 * */
fun parseCommand(input: String?): Pair<Command, List<String>>? {
	if (input == null)
		return null
	val text = input.trim()
	val command = when (text.split(' ').first()) {
		"content" -> Command.CONTENT
		"insert" -> Command.INSERT
		"update" -> Command.UPDATE
		"find" -> Command.FIND
		"findRegex" -> Command.FIND_REGEX
		"erase" -> Command.ERASE
		"eraseRegex" -> Command.ERASE_REGEX
		"exit" -> Command.EXIT
		"save" -> Command.SAVE
		"clear" -> Command.CLEAR
		"createGroup" -> Command.CREATE_GROUP
		"eraseGroup" -> Command.ERASE_GROUP
		"eraseFromGroup" -> Command.ERASE_FROM_GROUP
		"insertInGroup" -> Command.INSERT_IN_GROUP
		"findInGroup" -> Command.FIND_IN_GROUP
		"contentOfGroup" -> Command.CONTENT_OF_GROUP
		"contentOfAllGroups" -> Command.CONTENT_OF_ALL_GROUPS
		"rollback" -> Command.ROLLBACK
		else -> {
			val command = text.split(' ').first().lowercase()
			if (command == "")
				return null
			println("Incorrect command")
			val listOfAvailable = listOf(
				"content", "insert", "update", "find", "findRegex", "erase", "eraseRegex", "exit", "save", "clear",
				"createGroup", "eraseGroup", "eraseFromGroup", "insertInGroup", "findInGroup", "contentOfGroup",
				"contentOfAllGroups"
			)
			listOfAvailable.forEach {
				var countOfDifferent = 0
				for (i in 0 until maxOf(it.length, command.length))
					if (it.lowercase().getOrNull(i) != command.getOrNull(i))
						countOfDifferent++
				if (countOfDifferent < 3)
					println("Maybe you mean $it")
			}
			return null
		}
	}
	return when (command) {
		// these commands need two arguments separated by "->"
		Command.INSERT, Command.UPDATE, Command.INSERT_IN_GROUP, Command.ERASE_FROM_GROUP, Command.FIND_IN_GROUP -> {
			if (text.indexOf("->") == -1 || !text.contains(' ')) {
				println("Wrong count of arguments")
				null
			} else
				Pair(
					command,
					listOf(
						text.substring(text.indexOf(' ') until text.indexOf("->")).trim(),
						text.substring(text.indexOf("->") + 2 until text.length).trim()
					)
				)
		}
		// these commands need one argument
		Command.FIND, Command.FIND_REGEX, Command.ERASE, Command.ERASE_REGEX, Command.CONTENT_OF_GROUP,
		Command.CREATE_GROUP, Command.ERASE_GROUP -> {
			if (!text.contains(' ')) {
				println("Wrong count of arguments")
				null
			} else
				Pair(command, listOf(text.substring(text.indexOf(' ') until text.length).trim()))
		}
		// these commands do not need arguments
		Command.CLEAR, Command.SAVE, Command.EXIT, Command.CONTENT, Command.CONTENT_OF_ALL_GROUPS, Command.ROLLBACK -> {
			if (text.contains(' ')) {
				println("Wrong count of arguments")
				null
			} else
				Pair(command, listOf())
		}
	}
}

/**
 * Representation map in readable form.
 * key1 -> value1
 * key2 -> value2
 * ...
 * */
fun Map<String, String>.joinToString(): String {
	val result = toSortedMap().map { "${it.key} -> ${it.value}" }.joinToString(separator = "\n")
	return if (result == "") "Nothing" else result
}

fun askKeyFromUser(): String {
	print("Key word:")
	var answer: String
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
fun greeting(): Database {
	println("Hello!!!")
	print("Do you want to continue work with database or start with empty database?[Continue/Start]")
	var answer = readLine() ?: throw IllegalArgumentException("Answer hasn't been read")
	while (answer.lowercase() !in listOf("continue", "start")) {
		println("Choose from two option(Continue/Start):")
		answer = readLine() ?: throw IllegalArgumentException("Answer hasn't been read")
	}
	return if (answer.lowercase() == "continue") {
		var keyIsCorrect = false
		var res = Database(mutableMapOf(), mutableMapOf())
		while (!keyIsCorrect) {
			try {
				res = readBase(askKeyFromUser())
				keyIsCorrect = true
			} catch (e: IllegalAccessError) {
				println("Wrong key.")
			}
		}
		res
	} else {
		// key does not matter
		writeToBase(Database(mutableMapOf(), mutableMapOf()), "K")
		Database(mutableMapOf(), mutableMapOf())
	}
}

/**
 * This function organizes working process
 * argument is content of database.
 * there is the loop while in which program read command and call function which process it.
 * */
fun workingProcess(database: Database) {
	var exit = false
	val savesOperations = mutableListOf<Operation>()
	while (!exit) {
		print("write your command:")
		val command = parseCommand(readLine()) ?: continue
		val (isCorrect, message) = checkValidity(database, command)
		if (!isCorrect) {
			println(message)
			continue
		}
		if (command.first in setOf(
				Command.ERASE, Command.ERASE_REGEX, Command.UPDATE, Command.INSERT, Command.ERASE_GROUP,
				Command.CREATE_GROUP, Command.CLEAR, Command.ERASE_FROM_GROUP, Command.INSERT_IN_GROUP
			)
		)
			savesOperations.add(createOperationForCommand(database, command))
		when (command.first) {
			Command.FIND, Command.FIND_REGEX -> processFindCommand(database, command)
			Command.ERASE, Command.ERASE_REGEX -> processEraseCommand(database, command)
			Command.UPDATE, Command.INSERT -> processChangeCommand(database, command)
			Command.CONTENT -> println(database.content.joinToString())
			Command.ERASE_GROUP -> processEraseGroupCommand(database, command)
			Command.CREATE_GROUP -> processCreateGroupCommand(database, command)
			Command.CLEAR -> processClearCommand(database)
			Command.SAVE -> processSaveCommand(database)
			Command.CONTENT_OF_GROUP -> processContentOfGroupCommand(database, command)
			Command.CONTENT_OF_ALL_GROUPS -> processContentOfAllGroupsCommand(database)
			Command.FIND_IN_GROUP -> processFindInGroupCommand(database, command)
			Command.ERASE_FROM_GROUP -> processEraseFromGroupCommand(database, command)
			Command.INSERT_IN_GROUP -> processInsertInGroupCommand(database, command)
			Command.ROLLBACK -> processRollbackCommand(database, savesOperations)
			Command.EXIT -> exit = processExitCommand(database)
		}
	}
}

fun main(args: Array<String>) {
	try {
		val options = processArguments(args)
		if (options != null)
			workWithFile(options.first, options.second)
		else
			workingProcess(greeting())
	} catch (e: FileNotFoundException) {
		println(e.message)
	} catch (e: IllegalArgumentException) {
		println(e.message)
	}
}
