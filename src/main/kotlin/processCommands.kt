import java.util.regex.PatternSyntaxException

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

fun processRollBackCommand(cont: MutableMap<String, String>, operations: MutableList<Operation>){
	if (operations.isNotEmpty()) {
		val operation = operations.last()
		operation.inserted.forEach {
			cont.remove(it)
		}
		operation.erased.forEach {
			cont[it.first] = it.second
		}
		operations.removeLast()
		println("Done")
	} else
		println("Last operation is not saved (or not exist)")
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

fun processClearCommand(cont: MutableMap<String, String>, operations: MutableList<Operation>) {
	operations.add(Operation(cont.map { Pair(it.key, it.value) }, listOf()))
	cont.clear()
	println("Done")
}

fun processSaveCommand(cont: MutableMap<String, String>) {
	writeToBase(cont, askKeyFromUser())
	println("Done")
}

fun processExitCommand(cont: MutableMap<String, String>) {
	println("Do you want to save data?[Y/N]")
	val answer = readLine()
	if (answer != "N" && answer != "n" && answer != null) {
		writeToBase(cont, askKeyFromUser())
		println("Data has been saved.")
	}
}