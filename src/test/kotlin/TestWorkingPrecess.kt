import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals

/**
 * These tests check the whole script
 */
class TestWorkingPrecess {
	private val standardOut = System.out
	private val standardIn = System.`in`
	private val stream = ByteArrayOutputStream()

	@BeforeTest
	fun setUp() {
		System.setOut(PrintStream(stream))
	}

	@AfterTest
	fun tearDown() {
		System.setOut(standardOut)
		System.setIn(standardIn)
	}


	@Test
	fun testContent() {
		val database = Database(mutableMapOf("a" to "b", "c" to "b"), mutableMapOf())
		val output = executeCommand(database, "content")
		assertEquals("a -> b\nc -> b\n", output)
	}

	@Test
	fun testFind() {
		val database = Database(mutableMapOf("a" to "b", "c" to "b", "key" to "value"), mutableMapOf())
		val output = executeCommand(database, "find a\nfindRegex .")
		assertEquals("b\na -> b\nc -> b\n", output)
	}

	@Test
	fun testInsert() {
		val database = Database(mutableMapOf("a" to "b", "c" to "b"), mutableMapOf())
		val output = executeCommand(database, "insert new key -> new value\nfind new key")
		assertEquals("new value\n", output)
	}

	@Test
	fun testErase() {
		val database = Database(mutableMapOf("a" to "b", "c" to "b"), mutableMapOf())
		val output = executeCommand(database, "erase a\ncontent")
		assertEquals("c -> b\n", output)
	}

	@Test
	fun testEraseRegex() {
		val database = Database(mutableMapOf("a" to "b", "aa" to "b", "aaa" to "b"), mutableMapOf())
		val output = executeCommand(database, "eraseRegex a{2,}\ncontent")
		assertEquals("This field is removed\n[aa, aaa]\na -> b\n", output)
	}

	@Test
	fun testClear() {
		val database = Database(mutableMapOf("a" to "b", "aa" to "b"), mutableMapOf())
		val output = executeCommand(database, "content\nclear\ncontent")
		assertEquals("a -> b\naa -> b\nNothing\n", output)
	}

	@Test
	fun testUpdate() {
		val database = Database(mutableMapOf("a" to "b", "aa" to "b"), mutableMapOf())
		val output = executeCommand(database, "update aa -> bb\ncontent")
		assertEquals("a -> b\naa -> bb\n", output)
	}

	@Test
	fun testCreateEraseGroup() {
		val cont = mutableMapOf("a" to "b", "aa" to "b")
		val command = "createGroup gr1\ncontentOfGroup gr1\neraseGroup gr1\ncontentOfGroup gr1"
		val output = executeCommand(Database(cont, mutableMapOf()), command)
		assertEquals("Nothing\nGroup with this name does not exist\n", output)
	}

	@Test
	fun testInsertInGroup() {
		val database = Database(mutableMapOf("a" to "b", "aa" to "b"), mutableMapOf("gr1" to mutableListOf()))
		val command = "insertInGroup a -> gr1\ncontentOfGroup gr1"
		val output = executeCommand(database, command)
		assertEquals("a -> b\n", output)
	}

	@Test
	fun testEraseFromGroup() {
		val database = Database(mutableMapOf("a" to "b", "aa" to "b"), mutableMapOf("gr1" to mutableListOf("a", "aa")))
		val command = "eraseFromGroup gr1 -> aa\ncontentOfGroup gr1"
		val output = executeCommand(database, command)
		assertEquals("a -> b\n", output)
	}

	@Test
	fun testFindInGroup() {
		val database = Database(mutableMapOf("a" to "b", "c" to "b"), mutableMapOf("gr1" to mutableListOf("a")))
		val command = "findInGroup gr1 -> c\nfindInGroup gr1 -> a"
		val output = executeCommand(database, command)
		assertEquals("Group gr1 does not contain c\na -> b\n", output)
	}

	@Test
	fun testManyCommands() {
		val cont = mutableMapOf<String, String>()
		val commands = "insert a -> b\ninsert c -> long value\ncontent\ninsert ac -> bb\nfindRegex ."
		val output = executeCommand(Database(cont, mutableMapOf()), commands)
		assertEquals("a -> b\nc -> long value\na -> b\nc -> long value\n", output)
	}

	@Test
	fun testManyCommandsWithGroups() {
		val database = Database(mutableMapOf("a" to "b", "c" to "b"), mutableMapOf("gr1" to mutableListOf("a")))
		val command = "contentOfGroup gr1\ninsertInGroup c -> gr1\nerase a\ncontentOfGroup gr1"
		val output = executeCommand(database, command)
		assertEquals("a -> b\nc -> b\n", output)
	}

	@Test
	fun testIncorrectCommand() {
		val database = Database(
			mutableMapOf("a" to "b"),
			mutableMapOf()
		)
		val command = "blablabla\nbombombom\nInsert"
		val output = executeCommand(database, command)
		assertEquals("Incorrect command\nIncorrect command\nIncorrect command\nMaybe you mean insert\n", output)
	}

	@Test
	fun testWrongCountOfArguments() {
		val database = Database(
			mutableMapOf("a" to "b"),
			mutableMapOf()
		)
		val command = "insert a"
		val output = executeCommand(database, command)
		assertEquals("Wrong count of arguments\n", output)
	}

	@Test
	fun testWrongArgumentsForInsert() {
		val database = Database(
			mutableMapOf("a" to "b"),
			mutableMapOf()
		)
		val command = "insert a -> c"
		val output = executeCommand(database, command)
		assertEquals("Database has already contained this key\n", output)
	}

	@Test
	fun testWrongArgumentsForErase() {
		val database = Database(
			mutableMapOf("a" to "b"),
			mutableMapOf()
		)
		val command = "erase b"
		val output = executeCommand(database, command)
		assertEquals("Database does not contain this key\n", output)
	}

	@Test
	fun testWrongArgumentsForGroupCommands() {
		val database = Database(
			mutableMapOf("a" to "b"),
			mutableMapOf("gr1" to mutableListOf("a"))
		)
		val command =
			"createGroup gr1\neraseGroup gr2\ninsertInGroup a -> gr2\ninsertInGroup b -> gr1\ninsertInGroup a -> gr1"
		val output = executeCommand(database, command)
		assertEquals("Group with this name has already exist\n" +
				"Group with this name does not exist\n" +
				"Group with this name does not exist\n" +
				"This key does not exist\n" +
				"Group has already contained this key\n", output)
	}

	private fun executeCommand(database: Database, commands: String): String {
		System.setIn(ByteArrayInputStream(("$commands\nexit\nN\n").toByteArray()))
		workingProcess(database)
		return stream.toString().trim()
			.replace("write your command:".toRegex(), "").replace("\r", "")
			.replace("Do you want to save data?[Y/N]", "")
			.replace("Do you want to save data?[Y/N/Cancel]", "")
	}
}