import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class TestProcessWrongCommand {

	@Test
	fun testWrongArgumentsForInsert() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.INSERT, listOf("key1", "new value"))
		assertEquals(checkValidity(database, command), Pair(false, "Database has already contained this key"))
	}

	@Test
	fun testWrongArgumentsForErase() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.ERASE, listOf("key4"))
		assertEquals(checkValidity(database, command), Pair(false, "Database does not contain this key"))
	}

	@Test
	fun testWrongArgumentsForUpdate() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.UPDATE, listOf("key4"))
		assertEquals(checkValidity(database, command), Pair(false, "Database does not contain this key"))
	}

	@Test
	fun testWrongArgumentsForCreateGroup() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.CREATE_GROUP, listOf("group"))
		assertEquals(checkValidity(database, command), Pair(false, "Group with this name has already exist"))
	}

	@Test
	fun testWrongArgumentsForEraseGroup() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.ERASE_GROUP, listOf("group2"))
		assertEquals(checkValidity(database, command), Pair(false, "Group with this name does not exist"))
	}

	@Test
	fun testWrongArgumentsForInsertInGroup() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		var command = Pair(Command.INSERT_IN_GROUP, listOf("key1", "group"))
		assertEquals(checkValidity(database, command), Pair(false, "Group has already contained this key"))
		command = Pair(Command.INSERT_IN_GROUP, listOf("key4", "group"))
		assertEquals(checkValidity(database, command), Pair(false, "This key does not exist"))
		command = Pair(Command.INSERT_IN_GROUP, listOf("key2", "group1"))
		assertEquals(checkValidity(database, command), Pair(false, "Group with this name does not exist"))
	}

}

class TestProcessCorrectCommands {

	private val standardOut = System.out
	private val stream = ByteArrayOutputStream()

	@BeforeTest
	fun setUp() {
		System.setOut(PrintStream(stream))
	}

	@AfterTest
	fun tearDown() {
		System.setOut(standardOut)
	}

	@Test
	fun testFindProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.FIND, listOf("key1"))
		processFindCommand(database, command)
		assertEquals("value1\n", getOutput())
	}

	@Test
	fun testFindNotExistProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.FIND, listOf("key4"))
		processFindCommand(database, command)
		assertEquals("Database do not contain this key\n", getOutput())
	}

	@Test
	fun testFindRegexProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.FIND_REGEX, listOf("key[12]"))
		processFindCommand(database, command)
		assertEquals("key1 -> value1\nkey2 -> value2\n", getOutput())
	}

	@Test
	fun testInsertProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.INSERT, listOf("key4", "value4"))
		processChangeCommand(database, command)
		assertEquals("", getOutput())
		assertEquals(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3", "key4" to "value4"),
			database.content
		)
	}

	@Test
	fun testEraseProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.ERASE, listOf("key1"))
		processEraseCommand(database, command)
		assertEquals("", getOutput())
		assertEquals(
			mutableMapOf("key2" to "value2", "key3" to "value3"),
			database.content
		)
	}

	@Test
	fun testEraseRegexProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.ERASE_REGEX, listOf("key[23]"))
		processEraseCommand(database, command)
		assertEquals("This field is removed\n[key2, key3]\n", getOutput())
		assertEquals(
			mutableMapOf("key1" to "value1"),
			database.content
		)
	}

	@Test
	fun testClearProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		processClearCommand(database)
		assertEquals("", getOutput())
		assertEquals(Database(mutableMapOf(), mutableMapOf()), database)
	}

	@Test
	fun testCreateGroupProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.CREATE_GROUP, listOf("new group"))
		processCreateGroupCommand(database, command)
		assertEquals("", getOutput())
		assertEquals(
			Database(
				mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
				mutableMapOf("group" to mutableListOf("key1", "key3"), "new group" to mutableListOf())
			), database
		)
	}

	@Test
	fun testEraseGroupProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.ERASE_GROUP, listOf("group"))
		processEraseGroupCommand(database, command)
		assertEquals("", getOutput())
		assertEquals(
			Database(
				mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
				mutableMapOf()
			), database
		)
	}

	@Test
	fun testInsertInGroupProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.INSERT_IN_GROUP, listOf("key2", "group"))
		processInsertInGroupCommand(database, command)
		assertEquals("", getOutput())
		assertEquals(
			Database(
				mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
				mutableMapOf("group" to mutableListOf("key1", "key3", "key2"))
			), database
		)
	}

	@Test
	fun testEraseFromGroupProcess() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"))
		)
		val command = Pair(Command.ERASE_FROM_GROUP, listOf("group", "key1"))
		processEraseFromGroupCommand(database, command)
		assertEquals("", getOutput())
		assertEquals(
			Database(
				mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
				mutableMapOf("group" to mutableListOf("key3"))
			), database
		)
	}

	@Test
	fun testContentOfAllGroups() {
		val database = Database(
			mutableMapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
			mutableMapOf("group" to mutableListOf("key1", "key3"), "group2" to mutableListOf("key1"))
		)
		processContentOfAllGroupsCommand(database)
		assertEquals(
			"Name: group\nContent:\nkey1 -> value1\nkey3 -> value3\nName: group2\nContent:\nkey1 -> value1\n",
			getOutput()
		)
	}

	private fun getOutput() = stream.toString().replace("\r", "")
}