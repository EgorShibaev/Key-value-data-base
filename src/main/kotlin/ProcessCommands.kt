import java.util.regex.PatternSyntaxException

/**
 * All function in this file organize procession of commands.
 * Each function arguments are content of database and command.
 * */


/**
 * This function check are arguments of commands correct.
 * Function return Boolean value - is command correct and string - message about error.
 * If first value are true, message should be ignored
 * */
fun checkValidity(database: Database, command: Pair<Command, List<String>>): Pair<Boolean, String> {
	val args = command.second
	val content = database.content
	val groups = database.groups
	return when (command.first) {
		Command.INSERT -> Pair(!content.contains(args[0]), "Database has already contained this key")
		Command.ERASE -> Pair(content.contains(args[0]), "Database does not contain this key")
		Command.UPDATE -> Pair(content.contains(args[0]), "Database does not contain this key")
		Command.CREATE_GROUP -> Pair(!groups.contains(args[0]), "Group with this name has already exist")
		Command.ERASE_GROUP -> Pair(groups.contains(args[0]), "Group with this name does not exist")
		Command.INSERT_IN_GROUP -> when {
			!groups.contains(args[1]) -> Pair(false, "Group with this name does not exist")
			groups.getValue(args[1]).contains(args[0]) -> Pair(false, "Group has already contained this key")
			!content.contains(args[0]) -> Pair(false, "This key does not exist")
			else -> Pair(true, "")
		}
		Command.ERASE_FROM_GROUP -> when {
			!groups.contains(args[0]) -> Pair(false, "Group with this name does not exist")
			!groups.getValue(args[0]).contains(args[1]) -> Pair(false, "Group does not contain this key")
			!content.contains(args[1]) -> Pair(false, "This key does not exist")
			else -> Pair(true, "")
		}
		Command.ERASE_REGEX, Command.FIND_REGEX -> {
			var ok = true
			var message = ""
			try {
				args[0].toRegex()
			} catch (e: PatternSyntaxException) {
				ok = false
				message = e.message.toString()
			}
			Pair(ok, message)
		}
		Command.CONTENT_OF_GROUP -> Pair(groups.contains(args[0]), "Group with this name does not exist")
		Command.FIND_IN_GROUP -> Pair(groups.contains(args[0]), "Group with this name does not exist")
		else -> Pair(true, "")
	}
}

fun processEraseCommand(database: Database, command: Pair<Command, List<String>>) = when (command.first) {
	Command.ERASE -> {
		database.content.remove(command.second[0])
		database.groups.forEach {
			it.value.remove(command.second[0])
		}
	}
	else -> {
		val fieldsForRemove = database.content.keys.filter { it.matches(command.second[0].toRegex()) }
		database.content.minusAssign(fieldsForRemove)
		database.groups.forEach {
			fieldsForRemove.forEach { key ->
				it.value.remove(key)
			}
		}
		println("This field is removed\n$fieldsForRemove")
	}
}

fun processChangeCommand(database: Database, command: Pair<Command, List<String>>) = when (command.first) {
	Command.INSERT -> database.content[command.second[0]] = command.second[1]
	else -> database.content[command.second[0]] = command.second[1]
}


fun processFindCommand(database: Database, command: Pair<Command, List<String>>) = when (command.first) {
	Command.FIND -> when {
		database.content.containsKey(command.second[0]) -> println(database.content[command.second[0]])
		else -> println("Database do not contain this key")
	}
	else -> println(database.content.filter { it.key.matches(command.second[0].toRegex()) }.joinToString())

}

fun processClearCommand(database: Database) {
	database.content.clear()
	database.groups.clear()
}

fun processSaveCommand(database: Database) {
	writeToBase(database, askKeyFromUser())
}

fun processExitCommand(database: Database): Boolean {
	println("Do you want to save data?[Y/N/Cancel]")
	return when (readLine()) {
		"N", "n" -> true
		"Cancel", "cancel" -> false
		else -> {
			writeToBase(database, askKeyFromUser())
			println("Data has been saved.")
			true
		}
	}
}

fun processRollbackCommand(database: Database, operations: MutableList<Operation>) {
	if (operations.isEmpty())
		println("Last operation is not saved (or not exists).")
	else {
		operations.last().rollback(database)
		operations.removeLast()
	}
}

fun processCreateGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	database.groups[command.second[0]] = mutableListOf()
}

fun processEraseGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	database.groups.remove(command.second[0])
}

fun processInsertInGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	database.groups.getValue(command.second[1]).add(command.second[0])
}

fun processEraseFromGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	database.groups.getValue(command.second[0]).remove(command.second[1])
}

fun processFindInGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	when {
		database.groups.getValue(command.second[0]).contains(command.second[1]) ->
			println("${command.second[1]} -> ${database.content.getValue(command.second[1])}")
		else ->
			println("Group ${command.second[0]} does not contain ${command.second[1]}")
	}
}

fun processContentOfGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	val keys = database.groups.getValue(command.second[0])
	println(database.content.filter {
		it.key in keys
	}.joinToString())
}

fun processContentOfAllGroupsCommand(database: Database) {
	database.groups.forEach {
		println("Name: ${it.key}")
		println("Content:")
		println(database.content.filter { (key, _) ->
			key in it.value
		}.joinToString())
	}
}