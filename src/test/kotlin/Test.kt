import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals

class TestEncryptDecipher {

	@Test
	fun test1() {
		val text = "some text is here"
		val key = "key"
		assertEquals(text, decipher(encrypt(text, key), key))
	}

	@Test
	fun test2() {
		val text = "text\n with\n few lines\n "
		val key = "KEY"
		assertEquals(text, decipher(encrypt(text, key), key))
	}

	@Test
	fun test3() {
		val text = "one  two three for     five \n six seven     eight\n"
		val key = "some long key abcdef"
		assertEquals(text, decipher(encrypt(text, key), key))
	}

	@Test
	fun test4() {
		val text = "short text"
		val key = "this key is longer that text"
		assertEquals(text, decipher(encrypt(text, key), key))
	}

	@Test
	fun test5() {
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
		assertEquals(output, "a -> b\nc -> b\n")
	}

	@Test
	fun testFind() {
		val cont = mutableMapOf("a" to "b", "c" to "b", "key" to "value")
		val output = executeCommand(cont, "find 'a'\nfindRegex '.'")
		assertEquals(output, "b\na -> b\nc -> b\n")
	}

	@Test
	fun testSet() {
		val cont = mutableMapOf("a" to "b", "c" to "b")
		val output = executeCommand(cont, "set 'new key' 'new value'\nfind 'new key'")
		assertEquals(output, "Done\nnew value\n")
	}

	private fun executeCommand(cont: MutableMap<String, String>, commands: String): String {
		System.setIn(ByteArrayInputStream(("$commands\nexit\n").toByteArray()))
		workingProcess(cont)
		return stream.toString().trim().replace("write your command:".toRegex(), "")
	}
}
