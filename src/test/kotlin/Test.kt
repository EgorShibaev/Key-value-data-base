import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals

class TestEncryptDecipher {

	@Test
	fun testSomeText() {
		val text = "some text is here"
		val key = "key"
		assertEquals(text, decipher(encrypt(text, key), key))
	}

	@Test
	fun testTextInSeveralLines() {
		val text = "text\n with\n few lines\n "
		val key = "KEY"
		assertEquals(text, decipher(encrypt(text, key), key))
	}

	@Test
	fun testLongText() {
		val text = "one  two three for     five \n six seven     eight\n"
		val key = "some long key abcdef"
		assertEquals(text, decipher(encrypt(text, key), key))
	}

	@Test
	fun testLongKey() {
		val text = "short text"
		val key = "this key is longer that text"
		assertEquals(text, decipher(encrypt(text, key), key))
	}

	@Test
	fun testEmptyText() {
		val text = ""
		val key = "key"
		assertEquals(text, decipher(encrypt(text, key), key))
	}
}

class TestWorkingProcess {
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
		val cont = mutableMapOf("a" to "b", "c" to "b")
		val output = executeCommand(cont, "content")
		assertEquals("a -> b\nc -> b\n", output)
	}

	@Test
	fun testFind() {
		val cont = mutableMapOf("a" to "b", "c" to "b", "key" to "value")
		val output = executeCommand(cont, "find a\nfindRegex .")
		assertEquals("b\na -> b\nc -> b\n", output)
	}

	@Test
	fun testInsert() {
		val cont = mutableMapOf("a" to "b", "c" to "b")
		val output = executeCommand(cont, "insert new key -> new value\nfind new key")
		assertEquals("Done\nnew value\n", output)
	}

	@Test
	fun testErase() {
		val cont = mutableMapOf("a" to "b", "c" to "b")
		val output = executeCommand(cont, "erase a\ncontent")
		assertEquals("Done\nc -> b\n", output)
	}

	@Test
	fun testEraseRegex() {
		val cont = mutableMapOf("a" to "b", "aa" to "b", "aaa" to "b")
		val output = executeCommand(cont, "eraseRegex a{2,}\ncontent")
		assertEquals("This field is removed\n[aa, aaa]\na -> b\n", output)
	}

	@Test
	fun testClear() {
		val cont = mutableMapOf("a" to "b", "aa" to "b")
		val output = executeCommand(cont, "content\nclear\ncontent")
		assertEquals("a -> b\naa -> b\nDone\nBase is empty\n", output)
	}

	@Test
	fun testUpdate() {
		val cont = mutableMapOf("a" to "b", "aa" to "b")
		val output = executeCommand(cont, "update aa -> bb\ncontent")
		assertEquals("Done\na -> b\naa -> bb\n", output)
	}

	@Test
	fun testRollback() {
		val cont = mutableMapOf("a" to "b", "aa" to "b")
		val output = executeCommand(cont, "insert d -> e\nrollback\ncontent")
		assertEquals("Done\nDone\na -> b\naa -> b\n", output)
	}

	@Test
	fun testManyCommands() {
		val cont = mutableMapOf<String, String>()
		val commands = "insert a -> b\ninsert c -> long value\ncontent\ninsert ac -> bb\nfindRegex ."
		val output = executeCommand(cont, commands)
		assertEquals("Done\nDone\na -> b\nc -> long value\nDone\na -> b\nc -> long value\n", output)
	}

	private fun executeCommand(cont: MutableMap<String, String>, commands: String): String {
		System.setIn(ByteArrayInputStream(("$commands\nexit\nN\n").toByteArray()))
		workingProcess(cont)
		return stream.toString().trim()
			.replace("write your command:".toRegex(), "").replace("\r", "")
			.replace("Do you want to save data?[Y/N]", "")
	}
}
