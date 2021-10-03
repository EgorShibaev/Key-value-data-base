/**
 * In this file are classes which organize storage of operation and functions to roll back
 * this operation. There is base class Operation with abstract method rollback. For each
 * change command there is a class which inherits Operation with override method rollback.
 * */
sealed class Operation {
	abstract fun rollback(database: Database)
}

data class InsertOperation(val key: String) : Operation() {
	override fun rollback(database: Database) {
		database.content.remove(key)

	}
}

data class EraseOperation(val key: String, val value: String, val namesOfGroups: List<String>) : Operation() {
	override fun rollback(database: Database) {
		database.content[key] = value
		namesOfGroups.forEach {
			database.groups.getValue(it).add(key)
		}
	}
}

data class ClearOperation(val databaseBackUp: Database) : Operation() {
	override fun rollback(database: Database) {
		database.content.putAll(databaseBackUp.content)
		database.groups.putAll(databaseBackUp.groups)
	}
}

data class EraseRegexOperation(val listOfEraseOperation: List<EraseOperation>) : Operation() {
	override fun rollback(database: Database) {
		listOfEraseOperation.forEach {
			it.rollback(database)
		}
	}
}

data class CreateGroupOperation(val name: String) : Operation() {
	override fun rollback(database: Database) {
		database.groups.remove(name)
	}
}

data class EraseGroupOperation(val name: String, val content: List<String>) : Operation() {
	override fun rollback(database: Database) {
		database.groups[name] = content.toMutableList()
	}
}

data class InsertInGroupOperation(val key: String, val groupName: String) : Operation() {
	override fun rollback(database: Database) {
		database.groups.getValue(groupName).remove(key)
	}
}

data class EraseFromGroupOperation(val key: String, val groupName: String) : Operation() {
	override fun rollback(database: Database) {
		database.groups.getValue(groupName).add(key)
	}
}

data class UpdateOperation(val key: String, val oldValue : String) : Operation() {
	override fun rollback(database: Database) {
		database.content[key] = oldValue
	}
}

object NotAChangeOperation : Operation() {
	override fun rollback(database: Database) = Unit
}

/**
 * This function return for all command class with info which we need to roll back operation.
 * */
fun createOperationForCommand(database: Database, command: Pair<Command, List<String>>): Operation {
	val args = command.second
	val content = database.content
	val groups = database.groups
	return when (command.first) {
		Command.INSERT -> InsertOperation(args[0])
		Command.CLEAR -> ClearOperation(Database(HashMap(content), HashMap(groups)))
		Command.CREATE_GROUP -> CreateGroupOperation(args[0])
		Command.ERASE_GROUP -> EraseGroupOperation(args[0], groups.getValue(args[0]))
		Command.INSERT_IN_GROUP -> InsertInGroupOperation(args[0], args[1])
		Command.ERASE_FROM_GROUP -> EraseFromGroupOperation(args[1], args[0])
		Command.ERASE -> EraseOperation(
			args[0],
			content.getValue(args[0]),
			groups.filter { args[0] in it.value }.keys.toList()
		)
		Command.ERASE_REGEX ->
			EraseRegexOperation(database.content.keys.filter { it.matches(args[0].toRegex()) }.map {
				val value = content.getValue(it)
				val nameGroups = groups.filter { group -> it in group.value }.keys.toList()
				EraseOperation(it, value, nameGroups)
			})
		Command.UPDATE -> UpdateOperation(args[0], content.getValue(args[0]))
		else -> NotAChangeOperation
	}
}