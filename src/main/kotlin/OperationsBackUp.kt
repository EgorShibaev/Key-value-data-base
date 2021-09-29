/**
 * In this file are classes which organize storage of operation and functions to roll back
 * this operation. There are classes for each operation with method for rollback.
 * There is class Operation which contains nullable field of all extra classes.
 * */
data class Operation(
	val insertOperation: InsertOperation?,
	val eraseOperation: EraseOperation?,
	val clearOperation: ClearOperation?,
	val eraseRegexOperation: EraseRegexOperation?,
	val createGroupOperation: CreateGroupOperation?,
	val eraseGroupOperation: EraseGroupOperation?,
	val insertInGroupOperation: InsertInGroupOperation?,
	val eraseFromGroupOperation: EraseFromGroupOperation?
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
		database.content.putAll(HashMap(databaseBackUp.content))
		database.groups.putAll(HashMap(databaseBackUp.groups))
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
		Command.INSERT -> Operation(InsertOperation(args[0]), null, null, null, null, null, null, null)
		Command.ERASE -> Operation(
			null,
			EraseOperation(args[0], content.getValue(args[0]), groups.filter { args[0] in it.value }.keys.toList()),
			null, null, null, null, null, null
		)
		Command.CLEAR -> Operation(
			null, null,
			ClearOperation(Database(HashMap(database.content), HashMap(database.groups))), null, null, null, null, null
		)
		Command.ERASE_REGEX -> {
			val operation =
				EraseRegexOperation(database.content.keys.filter { it.matches(command.second[0].toRegex()) }.map {
					val value = content.getValue(it)
					val nameGroups = groups.filter { group -> args[0] in group.value }.keys.toList()
					EraseOperation(it, value, nameGroups)
				})
			Operation(null, null, null, operation, null, null, null, null)
		}
		Command.CREATE_GROUP -> Operation(null, null, null, null, CreateGroupOperation(args[0]), null, null, null)
		Command.ERASE_GROUP -> Operation(
			null, null, null, null, null,
			EraseGroupOperation(args[0], groups.getValue(args[0])), null, null
		)
		Command.INSERT_IN_GROUP -> Operation(
			null, null, null, null, null, null,
			InsertInGroupOperation(args[0], args[1]), null
		)
		Command.ERASE_FROM_GROUP -> Operation(
			null, null, null, null, null, null, null,
			EraseFromGroupOperation(args[1], args[0])
		)
		Command.UPDATE -> Operation(
			InsertOperation(args[0]),
			EraseOperation(args[0], content.getValue(args[0]), groups.filter { args[0] in it.value }.keys.toList()),
			null, null, null, null, null, null
		)
		else -> Operation(null, null, null, null, null, null, null, null)
	}
}