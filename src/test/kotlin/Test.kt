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

	@Test
	fun testWrongKey() {
		val text = "some text here"
		val key1 = "first   key"
		val key2 = "another key"
		try {
			decipher(encrypt(text, key1), key2)
			assert(false)
		}
		catch (e : IllegalAccessError) {
			assert(true)
		}
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
		val output = executeCommand(Database(cont, mutableMapOf()), "content")
		assertEquals("a -> b\nc -> b\n", output)
	}

	@Test
	fun testFind() {
		val cont = mutableMapOf("a" to "b", "c" to "b", "key" to "value")
		val output = executeCommand(Database(cont, mutableMapOf()), "find a\nfindRegex .")
		assertEquals("b\na -> b\nc -> b\n", output)
	}

	@Test
	fun testInsert() {
		val cont = mutableMapOf("a" to "b", "c" to "b")
		val output = executeCommand(Database(cont, mutableMapOf()), "insert new key -> new value\nfind new key")
		assertEquals("new value\n", output)
	}

	@Test
	fun testErase() {
		val cont = mutableMapOf("a" to "b", "c" to "b")
		val output = executeCommand(Database(cont, mutableMapOf()), "erase a\ncontent")
		assertEquals("c -> b\n", output)
	}

	@Test
	fun testEraseRegex() {
		val cont = mutableMapOf("a" to "b", "aa" to "b", "aaa" to "b")
		val output = executeCommand(Database(cont, mutableMapOf()), "eraseRegex a{2,}\ncontent")
		assertEquals("This field is removed\n[aa, aaa]\na -> b\n", output)
	}

	@Test
	fun testClear() {
		val cont = mutableMapOf("a" to "b", "aa" to "b")
		val output = executeCommand(Database(cont, mutableMapOf()), "content\nclear\ncontent")
		assertEquals("a -> b\naa -> b\nNothing\n", output)
	}

	@Test
	fun testUpdate() {
		val cont = mutableMapOf("a" to "b", "aa" to "b")
		val output = executeCommand(Database(cont, mutableMapOf()), "update aa -> bb\ncontent")
		assertEquals("a -> b\naa -> bb\n", output)
	}

	@Test
	fun testManyCommands() {
		val cont = mutableMapOf<String, String>()
		val commands = "insert a -> b\ninsert c -> long value\ncontent\ninsert ac -> bb\nfindRegex ."
		val output = executeCommand(Database(cont, mutableMapOf()), commands)
		assertEquals("a -> b\nc -> long value\na -> b\nc -> long value\n", output)
	}

	private fun executeCommand(database: Database, commands: String): String {
		System.setIn(ByteArrayInputStream(("$commands\nexit\nN\n").toByteArray()))
		workingProcess(database)
		return stream.toString().trim()
			.replace("write your command:".toRegex(), "").replace("\r", "")
			.replace("Do you want to save data?[Y/N]", "")
	}
}
