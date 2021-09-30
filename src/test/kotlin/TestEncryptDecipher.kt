import org.junit.jupiter.api.Test
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
		} catch (e: IllegalAccessError) {
			assert(true)
		}
	}
}
