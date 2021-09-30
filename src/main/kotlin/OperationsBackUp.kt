/**
 * In this file are classes which organize storage of operation and functions to roll back
 * this operation. There are classes for each operation with method for rollback.
 * There is class Operation which contains nullable field of all extra classes.
 * */
data class Operation(
	val insertOperation: InsertOperation? = null,
	val eraseOperation: EraseOperation? = null,
	val clearOperation: ClearOperation? = null,
	val eraseRegexOperation: EraseRegexOperation? = null,
	val createGroupOperation: CreateGroupOperation? = null,
	val eraseGroupOperation: EraseGroupOperation? = null,
	val insertInGroupOperation: InsertInGroupOperation? = null,
	val eraseFromGroupOperation: EraseFromGroupOperation? = null
) {
	fun rollback(database: Database) {
		insertOperation?.rollback(database)
		eraseOperation?.rollback(database)
		clearOperation?.rollback(database)
		eraseRegexOperation?.rollback(database)
		createGroupOperation?.rollback(database)
		eraseGroupOperation?.rollback(database)
		insertInGroupOperation?.rollback(database)
		eraseFromGroupOperation?.rollback(database)
	}
}

data class InsertOperation(val key: String) {
	fun rollback(database: Database) {
		database.content.remove(key)
	}
}

data class EraseOperation(val key: String, val value: String, val namesOfGroups: List<String>) {
	fun rollback(database: Database) {
		database.content[key] = value
		namesOfGroups.forEach {
			database.groups.getValue(it).add(key)
		}
	}
}

data class ClearOperation(val databaseBackUp: Database) {
	fun rollback(database: Database) {
		database.content.putAll(databaseBackUp.content)
		database.groups.putAll(databaseBackUp.groups)
	}
}

data class EraseRegexOperation(val listOfEraseOperation: List<EraseOperation>) {
	fun rollback(database: Database) {
		listOfEraseOperation.forEach {
			it.rollback(database)
		}
	}
}

data class CreateGroupOperation(val name: String) {
	fun rollback(database: Database) {
		database.groups.remove(name)
	}
}

data class EraseGroupOperation(val name: String, val content: List<String>) {
	fun rollback(database: Database) {
		database.groups[name] = content.toMutableList()
	}
}

data class InsertInGroupOperation(val key: String, val groupName: String) {
	fun rollback(database: Database) {
		database.groups.getValue(groupName).remove(key)
	}
}

data class EraseFromGroupOperation(val key: String, val groupName: String) {
	fun rollback(database: Database) {
		database.groups.getValue(groupName).add(key)
	}
}

/**
 * This function return for all command class with info which we need to roll back operation.
 * */
fun createOperationForCommand(database: Database, command: Pair<Command, List<String>>): Operation {
	val args = command.second
	val content = database.content
	val groups = database.groups
	return when (command.first) {
		Command.INSERT -> Operation(InsertOperation(args[0]))
		Command.CLEAR -> Operation(clearOperation = ClearOperation(Database(HashMap(content), HashMap(groups))))
		Command.CREATE_GROUP -> Operation(createGroupOperation = CreateGroupOperation(args[0]))
		Command.ERASE_GROUP -> Operation(eraseGroupOperation = EraseGroupOperation(args[0], groups.getValue(args[0])))
		Command.INSERT_IN_GROUP -> Operation(insertInGroupOperation = InsertInGroupOperation(args[0], args[1]))
		Command.ERASE_FROM_GROUP -> Operation(eraseFromGroupOperation = EraseFromGroupOperation(args[1], args[0]))
		Command.ERASE -> Operation(
			eraseOperation = EraseOperation(
				args[0],
				content.getValue(args[0]),
				groups.filter { args[0] in it.value }.keys.toList()
			)
		)
		Command.ERASE_REGEX ->
			Operation(
				eraseRegexOperation =
				EraseRegexOperation(database.content.keys.filter { it.matches(command.second[0].toRegex()) }.map {
					val value = content.getValue(it)
					val nameGroups = groups.filter { group -> args[0] in group.value }.keys.toList()
					EraseOperation(it, value, nameGroups)
				})
			)
		Command.UPDATE -> Operation(
			InsertOperation(args[0]),
			EraseOperation(args[0], content.getValue(args[0]), groups.filter { args[0] in it.value }.keys.toList())
		)
		else -> Operation()
	}
}