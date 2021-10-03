import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestRollBack {

	@Test
	fun testRollbackInsert() {
		val database = Database(mutableMapOf("a" to "b"), mutableMapOf())
		val operation = InsertOperation("a")
		processRollbackCommand(database, mutableListOf(operation))
		assert(database.content.isEmpty())
		assert(database.groups.isEmpty())
	}

	@Test
	fun testRollbackErase() {
		val database = Database(
			mutableMapOf("a" to "b"),
			mutableMapOf("group1" to mutableListOf(), "group2" to mutableListOf())
		)
		val operation = EraseOperation("b", "old value", listOf("group1", "group2"))
		processRollbackCommand(database, mutableListOf(operation))
		assertEquals(mutableMapOf("a" to "b", "b" to "old value"), database.content)
		assertEquals(mutableMapOf("group1" to mutableListOf("b"), "group2" to mutableListOf("b")), database.groups)
	}

	@Test
	fun testRollbackUpdate() {
		val database = Database(mutableMapOf("a" to "b"), mutableMapOf("group1" to mutableListOf("a")))
		val operation = UpdateOperation("a", "old value")
		processRollbackCommand(database, mutableListOf(operation))
		assertEquals(mutableMapOf("a" to "old value"), database.content)
		assertEquals(mutableMapOf("group1" to mutableListOf("a")), database.groups)
	}

	@Test
	fun testRollbackEraseRegex() {
		val database = Database(
			mutableMapOf("aa" to "bb"),
			mutableMapOf("group1" to mutableListOf(), "group2" to mutableListOf())
		)
		val operationEraseB = EraseOperation("b", "old value b", listOf("group1"))
		val operationEraseA = EraseOperation("a", "old value a", listOf("group2"))
		val operation = EraseRegexOperation(listOf(operationEraseA, operationEraseB))
		processRollbackCommand(database, mutableListOf(operation))
		assertEquals(mutableMapOf("a" to "old value a", "aa" to "bb", "b" to "old value b"), database.content)
		assertEquals(mutableMapOf("group2" to mutableListOf("a"), "group1" to mutableListOf("b")), database.groups)
	}

	@Test
	fun testRollbackClear() {
		val database = Database(mutableMapOf(), mutableMapOf())
		val operation = ClearOperation(
			Database(
				mutableMapOf("a" to "aa", "b" to "bb", "c" to "cc"),
				mutableMapOf("group1" to mutableListOf("b", "c"))
			)
		)
		processRollbackCommand(database, mutableListOf(operation))
		assertEquals(mutableMapOf("a" to "aa", "b" to "bb", "c" to "cc"), database.content)
		assertEquals(mutableMapOf("group1" to mutableListOf("b", "c")), database.groups)
	}

	@Test
	fun testRollbackCreateGroup() {
		val database = Database(mutableMapOf("a" to "b"), mutableMapOf("group1" to mutableListOf()))
		val operation = CreateGroupOperation("group1")
		processRollbackCommand(database, mutableListOf(operation))
		assertEquals(mutableMapOf("a" to "b"), database.content)
		assertEquals(mutableMapOf(), database.groups)
	}

	@Test
	fun testRollbackEraseGroup() {
		val database = Database(mutableMapOf("a" to "b", "c" to "d"), mutableMapOf())
		val operation = EraseGroupOperation("group", mutableListOf("a"))
		processRollbackCommand(database, mutableListOf(operation))
		assertEquals(mutableMapOf("a" to "b", "c" to "d"), database.content)
		assertEquals(mutableMapOf("group" to mutableListOf("a")), database.groups)
	}

	@Test
	fun testRollbackInsertInGroup() {
		val database = Database(
			mutableMapOf("a" to "b", "c" to "d", "e" to "f"),
			mutableMapOf("gr1" to mutableListOf("c"))
		)
		val operation = InsertInGroupOperation("c", "gr1")
		processRollbackCommand(database, mutableListOf(operation))
		assertEquals(mutableMapOf("a" to "b", "c" to "d", "e" to "f"), database.content)
		assertEquals(mutableMapOf("gr1" to mutableListOf()), database.groups)
	}

	@Test
	fun testRollbackEraseFromGroup() {
		val database = Database(
			mutableMapOf("a" to "b", "c" to "d", "e" to "f"),
			mutableMapOf("gr1" to mutableListOf("c"))
		)
		val operation = EraseFromGroupOperation("e", "gr1")
		processRollbackCommand(database, mutableListOf(operation))
		assertEquals(mutableMapOf("a" to "b", "c" to "d", "e" to "f"), database.content)
		assertEquals(mutableMapOf("gr1" to mutableListOf("c", "e")), database.groups)
	}

	@Test
	fun testRollbackManyInsertEraseCommands() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2"),
			mutableMapOf("gr1" to mutableListOf())
		)
		// insert a -> b
		val insertOperation = InsertOperation("a")
		// update a -> new value
		val updateOperation = UpdateOperation("a", "b")
		// erase a
		val eraseOperation = EraseOperation("a", "new value", listOf())
		val operations = mutableListOf(insertOperation, updateOperation, eraseOperation)
		repeat(operations.size) {
			processRollbackCommand(database, operations)
		}
		assertEquals(mutableMapOf("key1" to "value1", "key2" to "value2"), database.content)
		assertEquals(mutableMapOf("gr1" to mutableListOf()), database.groups)
	}

	@Test
	fun testRollbackManyGroupsCommands() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2"),
			mutableMapOf("new group" to mutableListOf())
		)
		val createGroupOperation = CreateGroupOperation("new group")
		val insertInGroupOperation = InsertInGroupOperation("key1", "new group")
		val eraseFromGroupOperation = EraseFromGroupOperation("key1", "new group")
		val operations = mutableListOf(createGroupOperation, insertInGroupOperation, eraseFromGroupOperation)
		repeat(operations.size) {
			processRollbackCommand(database, operations)
		}
		assertEquals(mutableMapOf("key1" to "value1", "key2" to "value2"), database.content)
		assertEquals(mutableMapOf(), database.groups)
	}

	@Test
	fun testRollbackManyDifferentCommands() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("old group" to mutableListOf(), "new group" to mutableListOf("b"))
		)
		val createGroupOperation = CreateGroupOperation("new group")
		val insertInGroupAOperation = InsertInGroupOperation("a", "new group")
		val eraseOperation = EraseOperation("a", "old value", listOf("new group"))
		val insertBOperation = InsertOperation("b")
		val insertInGroupBOperation = InsertInGroupOperation("b", "new group")
		val insertCOperation = InsertOperation("c")
		val operations = mutableListOf(
			createGroupOperation, insertInGroupAOperation, eraseOperation, insertBOperation,
			insertInGroupBOperation, insertCOperation
		)
		repeat(operations.size) {
			processRollbackCommand(database, operations)
		}
		assertEquals(mutableMapOf("key1" to "value1", "a" to "old value"), database.content)
		assertEquals(mutableMapOf("old group" to mutableListOf()), database.groups)
	}

	@Test
	fun testCreateInsertOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("old group" to mutableListOf(), "new group" to mutableListOf("b"))
		)
		val command = Pair(Command.INSERT, listOf("key", "value"))
		val operation = createOperationForCommand(database, command) as? InsertOperation ?: assert(false)
		assertEquals(InsertOperation("key"), operation)
	}

	@Test
	fun testCreateEraseOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("group" to mutableListOf("key1", "b"))
		)
		val command = Pair(Command.ERASE, listOf("key1"))
		val operation = createOperationForCommand(database, command) as? EraseOperation ?: assert(false)
		assertEquals(EraseOperation("key1", "value1", listOf("group")), operation)
	}

	@Test
	fun testCreateClearOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("group" to mutableListOf("key1", "b"))
		)
		val command = Pair(Command.CLEAR, listOf<String>())
		val operation = createOperationForCommand(database, command) as? ClearOperation ?: assert(false)
		assertEquals(ClearOperation(database), operation)
	}

	@Test
	fun testCreateCreateGroupOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("group" to mutableListOf("key1", "b"))
		)
		val command = Pair(Command.CREATE_GROUP, listOf("new group"))
		val operation = createOperationForCommand(database, command) as? CreateGroupOperation ?: assert(false)
		assertEquals(CreateGroupOperation("new group"), operation)
	}

	@Test
	fun testCreateEraseGroupOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("group" to mutableListOf("key1", "b"))
		)
		val command = Pair(Command.ERASE_GROUP, listOf("group"))
		val operation = createOperationForCommand(database, command) as? EraseGroupOperation ?: assert(false)
		assertEquals(EraseGroupOperation("group", mutableListOf("key1", "b")), operation)
	}

	@Test
	fun testCreateInsertInGroupOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("group" to mutableListOf("key1", "b"))
		)
		val command = Pair(Command.INSERT_IN_GROUP, listOf("c", "group"))
		val operation = createOperationForCommand(database, command) as? InsertInGroupOperation ?: assert(false)
		assertEquals(InsertInGroupOperation("c", "group"), operation)
	}

	@Test
	fun testCreateEraseFromGroupOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("group" to mutableListOf("key1", "b"))
		)
		val command = Pair(Command.ERASE_FROM_GROUP, listOf("group", "key1"))
		val operation = createOperationForCommand(database, command) as? EraseFromGroupOperation ?: assert(false)
		assertEquals(EraseFromGroupOperation("key1", "group"), operation)
	}

	@Test
	fun testCreateUpdateOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("group" to mutableListOf("key1", "b"))
		)
		val command = Pair(Command.UPDATE, listOf("key1", "new value"))
		val operation = createOperationForCommand(database, command) as? UpdateOperation ?: assert(false)
		assertEquals(UpdateOperation("key1", "value1"), operation)
	}

	@Test
	fun testCreateEraseRegexOperation() {
		val database = Database(
			mutableMapOf("key1" to "value1", "c" to "value c", "b" to "value b"),
			mutableMapOf("group" to mutableListOf("key1", "b"))
		)
		val command = Pair(Command.ERASE_REGEX, listOf("."))
		val operation = createOperationForCommand(database, command) as? EraseRegexOperation ?: assert(false)
		assertEquals(
			EraseRegexOperation(
				listOf(
					EraseOperation("c", "value c", listOf()),
					EraseOperation("b", "value b", listOf("group"))
				)
			), operation
		)
	}
}