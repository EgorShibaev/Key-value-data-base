import java.util.regex.PatternSyntaxException

/**
 * This function has three arguments:
 * cont - content of database
 * command - command (erase or eraseRegex) and arguments of this command
 * operations - list of operation in which program will add current operation
 * */
fun processEraseCommand(
	database: Database,
	command: Pair<Command, List<String>>
) = when (command.first) {
	Command.ERASE -> when {
		database.content.containsKey(command.second[0]) -> {
			database.content.remove(command.second[0])
			database.groups.forEach {
				it.value.remove(command.second[0])
			}
		}
		else -> println("Database do not contain this key")
	}
	else -> {
		try {
			val fieldsForRemove = database.content.keys.filter { it.matches(command.second[0].toRegex()) }
			database.content.minusAssign(fieldsForRemove)
			database.groups.forEach {
				fieldsForRemove.forEach { key ->
					it.value.remove(key)
				}
			}
			println("This field is removed\n$fieldsForRemove")
		} catch (e: PatternSyntaxException) {
			println("Error: ${e.message}")
		}
	}
}

// arguments are the same as in processEraseCommand
fun processChangeCommand(
	database: Database,
	command: Pair<Command, List<String>>,
) = when (command.first) {
	Command.INSERT -> {
		if (database.content.containsKey(command.second[0]))
			println("Database contains this key")
		else {
			database.content[command.second[0]] = command.second[1]
		}
	}
	else -> {
		if (!database.content.containsKey(command.second[0]))
			println("Database does not contain this key")
		else {
			database.content[command.second[0]] = command.second[1]
		}
	}
}


fun processFindCommand(database: Database, command: Pair<Command, List<String>>) = when (command.first) {
	Command.FIND -> when {
		database.content.containsKey(command.second[0]) -> println(database.content[command.second[0]])
		else -> println("Database do not contain this key")
	}
	else -> try {
		println(database.content.filter { it.key.matches(command.second[0].toRegex()) }.joinToString())
	} catch (e: PatternSyntaxException) {
		println("Error: ${e.message}")
	}
}

fun processClearCommand(database: Database) {
	database.content.clear()
}

fun processSaveCommand(database: Database) {
	writeToBase(database, askKeyFromUser())
}

fun processExitCommand(database: Database) {
	println("Do you want to save data?[Y/N]")
	val answer = readLine()
	if (answer != "N" && answer != "n" && answer != null) {
		writeToBase(database, askKeyFromUser())
		println("Data has been saved.")
	}
}

fun processCreateGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	if (database.groups.containsKey(command.second[0]))
		println("Group with name ${command.second[0]} exist")
	else
		database.groups[command.second[0]] = mutableListOf()
}

fun processEraseGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	if (!database.groups.containsKey(command.second[0]))
		println("Group with name ${command.second[0]} do not exist")
	else
		database.groups.remove(command.second[0])
}

fun processInsertInGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	when {
		!database.groups.containsKey(command.second[1]) -> println("Group with name ${command.second[1]} do not exist")
		database.groups.getValue(command.second[1]).contains(command.second[0]) ->
			println("Group ${command.second[1]} contain ${command.second[0]}")
		!database.content.containsKey(command.second[0]) ->
			println("Database does not contain this key")
		else -> {
			database.groups.getValue(command.second[1]).add(command.second[0])
		}
	}
}

fun processEraseFromGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	when {
		!database.groups.containsKey(command.second[0]) -> println("Group with name ${command.second[0]} do not exist")
		!database.groups.getValue(command.second[0]).contains(command.second[1]) ->
			println("Group ${command.second[0]} do not contain ${command.second[1]}")
		!database.content.containsKey(command.second[1]) ->
			println("Database does not contain this key")
		else -> {
			database.groups.getValue(command.second[0]).remove(command.second[1])
		}
	}
}

fun processFindInGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	when {
		!database.groups.containsKey(command.second[0]) -> println("Group with name ${command.second[0]} do not exist")
		database.groups.getValue(command.second[0]).contains(command.second[1]) ->
			println("${command.second[1]} -> ${database.content.getValue(command.second[1])}")
		else ->
			println("Group ${command.second[0]} do not contain ${command.second[1]}")
	}
}

fun processContentOfGroupCommand(database: Database, command: Pair<Command, List<String>>) {
	when {
		!database.groups.containsKey(command.second[0]) -> println("Group with name ${command.second[0]} do not exist")
		else -> {
			val keys = database.groups.getValue(command.second[0])
			println(database.content.filter {
				it.key in keys
			}.joinToString())
		}
	}
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