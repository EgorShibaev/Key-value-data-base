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
		assertEquals(null, command)
		assertEquals("", getOutput())
	}

	@Test
	fun testEmptyInput() {
		val input = ""
		val command = parseCommand(input)
		assertEquals(null, command)
		assertEquals("", getOutput())
	}

	@Test
	fun testEmptyValue() {
		val input = "insert a -> "
		val command = parseCommand(input)
		assertEquals(Pair(Command.INSERT, listOf("a", "")), command)
		assertEquals("", getOutput())
	}

	@Test
	fun testArrowInInput() {
		val input = "insert a -> b -> c -> d"
		val command = parseCommand(input)
		assertEquals(Pair(Command.INSERT, listOf("a", "b -> c -> d")), command)
		assertEquals("", getOutput())
	}

	@Test
	fun testIncorrectInput() {
		val input = "bom bom bom"
		val command = parseCommand(input)
		assertEquals(null, command)
		assertEquals("Incorrect command\n", getOutput())
	}

	@Test
	fun testOneTypoInput() {
		val input = "contentOfAllGroUps"
		val command = parseCommand(input)
		assertEquals(command, null)
		assertEquals("Incorrect command\nMaybe you mean contentOfAllGroups\n", getOutput())
	}

	@Test
	fun testTwoTypoInput() {
		val input = "insel a -> b"
		val command = parseCommand(input)
		assertEquals(null, command)
		assertEquals("Incorrect command\nMaybe you mean insert\n", getOutput())
	}

	@Test
	fun testTreeTypoInput() {
		val input = "er??? a"
		val command = parseCommand(input)
		assertEquals(null, command)
		assertEquals("Incorrect command\n", getOutput())
	}

	@Test
	fun testCorrectInsert() {
		val input = "insert a -> b"
		val command = parseCommand(input)
		assertEquals(Pair(Command.INSERT, listOf("a", "b")), command)
		assertEquals("", getOutput())
	}

	@Test
	fun testCorrectErase() {
		val input = "erase key with spaces"
		val command = parseCommand(input)
		assertEquals(Pair(Command.ERASE, listOf("key with spaces")), command)
		assertEquals("", getOutput())
	}

	@Test
	fun testCorrectUpdate() {
		val input = "   	update key 1 -> value 1  "
		val command = parseCommand(input)
		assertEquals(Pair(Command.UPDATE, listOf("key 1", "value 1")), command)
		assertEquals("", getOutput())
	}

	@Test
	fun testCorrectFindRegex() {
		val input = "findRegex .{4,}"
		val command = parseCommand(input)
		assertEquals(Pair(Command.FIND_REGEX, listOf(".{4,}")), command)
		assertEquals("", getOutput())
	}

	@Test
	fun testCorrectContent() {
		val input = "content"
		val command = parseCommand(input)
		assertEquals(Pair(Command.CONTENT, listOf()), command)
		assertEquals("", getOutput())
	}

	@Test
	fun testWrongCountOfArgumentsForInsert() {
		val input = "insert a"
		val command = parseCommand(input)
		assertEquals(null, command)
		assertEquals("Wrong count of arguments\n", getOutput())
	}

	@Test
	fun testWrongCountOfArgumentsForContentOfAllGroups() {
		val input = "contentOfAllGroups group"
		val command = parseCommand(input)
		assertEquals(null, command)
		assertEquals("Wrong count of arguments\n", getOutput())
	}

	@Test
	fun testWrongCountOfArgumentsForEraseRegex() {
		val input = "eraseRegex  "
		val command = parseCommand(input)
		assertEquals(null, command)
		assertEquals("Wrong count of arguments\n", getOutput())
	}

	private fun getOutput() = stream.toString().replace("\r", "")
}