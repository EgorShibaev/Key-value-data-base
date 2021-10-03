import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class TestInputFromFile {
	@Test
	fun testInputFromFileTestDifferentCommands() {
		val inputFileName = "tests/testDifferentCommands/input.txt"
		val outputFileName = "tests/testDifferentCommands/output.txt"
		main(arrayOf("--file", inputFileName, outputFileName))
		assertEquals(
			"""
			a -> b
			b -> c
			c -> d
			a -> b
			b -> c
			b -> c
			Group does not contain this key
			b -> c
			c -> d
			
		""".trimIndent(), File(outputFileName).readText()
		)
	}

	@Test
	fun testInputFromFileTestEncryptDecipher() {
		val inputPart1FileName = "tests/testEncryptDecipher/startWithEmpty/input.txt"
		val outputPart1FileName = "tests/testEncryptDecipher/startWithEmpty/output.txt"
		main(arrayOf("--file", inputPart1FileName, outputPart1FileName))
		assertEquals(
			"""
			d
			Data has been saved.

		""".trimIndent(), File(outputPart1FileName).readText()
		)
		val inputPart2FileName = "tests/testEncryptDecipher/startWithSaved/input.txt"
		val outputPart2FileName = "tests/testEncryptDecipher/startWithSaved/output.txt"
		main(arrayOf("--file", inputPart2FileName, outputPart2FileName))
		assertEquals(
			"""
			a -> a
			b -> b
			c -> c
			d -> d
			a -> a
			b -> b
			c -> c
			d -> d
			
		""".trimIndent(), File(outputPart2FileName).readText()
		)
	}

	@Test
	fun testInputFromFileTestManyInserts() {
		val inputFileName = "tests/testManyInserts/input.txt"
		val outputFileName = "tests/testManyInserts/output.txt"
		main(arrayOf("--file", inputFileName, outputFileName))
		val expected = (0..49).map { "a$it -> b" }.sorted().joinToString(separator = "\n") + "\n"
		assertEquals(expected, File(outputFileName).readText())
	}

	@Test
	fun testInputFromFileWrongCommands() {
		val inputFileName = "tests/testWrongCommands/input.txt"
		val outputFileName = "tests/testWrongCommands/output.txt"
		main(arrayOf("--file", inputFileName, outputFileName))
		val expected = """
			Database does not contain this key
			Database has already contained this key
			Database does not contain this key
			Group with this name does not exist
			This key does not exist
			Group with this name does not exist
			Group does not contain this key
			Group with this name does not exist
			Group with this name does not exist
			Incorrect command
			Maybe you mean insert
			Wrong count of arguments
			Wrong count of arguments
			
		""".trimIndent()
		assertEquals(expected, File(outputFileName).readText())
	}
}