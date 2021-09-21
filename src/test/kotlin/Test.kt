import org.junit.jupiter.api.Test
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
