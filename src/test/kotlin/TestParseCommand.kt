import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.AfterTest

class TestParseCommand {
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
	fun testNullInput() {
		val input = null
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "")
	}

	@Test
	fun testEmptyInput() {
		val input = ""
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "")
	}

	@Test
	fun testIncorrectInput() {
		val input = "bom bom bom"
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "Incorrect command\n")
	}

	@Test
	fun testOneTypoInput() {
		val input = "contentOfAllGroUps"
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "Incorrect command\nMaybe you mean contentOfAllGroups\n")
	}

	@Test
	fun testTwoTypoInput() {
		val input = "insel a -> b"
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "Incorrect command\nMaybe you mean insert\n")
	}

	@Test
	fun testTreeTypoInput() {
		val input = "er??? a"
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "Incorrect command\n")
	}

	@Test
	fun testCorrectInsert() {
		val input = "insert a -> b"
		val command = parseCommand(input)
		assertEquals(command, Pair(Command.INSERT, listOf("a", "b")))
		assertEquals(getOutput(), "")
	}

	@Test
	fun testCorrectErase() {
		val input = "erase key with spaces"
		val command = parseCommand(input)
		assertEquals(command, Pair(Command.ERASE, listOf("key with spaces")))
		assertEquals(getOutput(), "")
	}

	@Test
	fun testCorrectUpdate() {
		val input = "   	update key 1 -> value 1  "
		val command = parseCommand(input)
		assertEquals(command, Pair(Command.UPDATE, listOf("key 1", "value 1")))
		assertEquals(getOutput(), "")
	}

	@Test
	fun testCorrectFindRegex() {
		val input = "findRegex .{4,}"
		val command = parseCommand(input)
		assertEquals(command, Pair(Command.FIND_REGEX, listOf(".{4,}")))
		assertEquals(getOutput(), "")
	}

	@Test
	fun testCorrectContent() {
		val input = "content"
		val command = parseCommand(input)
		assertEquals(command, Pair(Command.CONTENT, listOf()))
		assertEquals(getOutput(), "")
	}

	@Test
	fun testWrongCountOfArgumentsForInsert() {
		val input = "insert a"
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "Wrong count of arguments\n")
	}

	@Test
	fun testWrongCountOfArgumentsForContentOfAllGroups() {
		val input = "contentOfAllGroups group"
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "Wrong count of arguments\n")
	}

	@Test
	fun testWrongCountOfArgumentsForEraseRegex() {
		val input = "eraseRegex  "
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals(getOutput(), "Wrong count of arguments\n")
	}

	private fun getOutput() = stream.toString().replace("\r", "")
}