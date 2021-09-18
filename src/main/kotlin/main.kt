fun parseCommand(text: String?): List<String>? {
    if (text == null)
        return null
    val result = mutableListOf(text.split(' ').first())
    var lastQuotes: Char? = null
    var indexOfLastQuotes: Int = -1
    for (i in result[0].length until text.length)
        when {
            text[i] == lastQuotes -> {
                result.add(text.substring(indexOfLastQuotes + 1, i))
                lastQuotes = null
            }
            lastQuotes == null && text[i] in listOf('\'', '"') -> {
                lastQuotes = text[i]
                indexOfLastQuotes = i
            }
            lastQuotes == null && text[i] !in listOf('\'', '"', ' ') -> return null
        }
    return result
}

fun Map<String, String>.joinToString(): String {
    return map { "${it.key} -> ${it.value}" }.joinToString(separator = "\n")
}

fun main() {
    val cont = readBase().toMutableMap()
    while (true) {
        print("write your command:")
        val command = parseCommand(readLine())
        if (command == null) {
            println("Incorrect command")
            continue
        }
        when (command[0]) {
            "find" -> when {
                command.size != 2 -> println("Incorrect command")
                cont.containsKey(command[1]) -> println(cont[command[1]])
                else -> println("Database do not contain this key")
            }
            "set" -> when {
                command.size != 3 -> println("Incorrect command")
                else -> {
                    cont[command[1]] = command[2]
                    println("Done")
                }
            }
            "erase" -> when {
                command.size != 2 -> println("Incorrect command")
                cont.containsKey(command[1]) -> {
                    cont.remove(command[1])
                    println("Done")
                }
                else -> println("Database do not contain this key")
            }
            "eraseRegex" -> when {
                command.size != 2 -> println("Incorrect command")
                else -> {
                    cont.minusAssign(cont.keys.filter { it.matches(command[1].toRegex()) })
	                println("Done")
                }
            }
            "content" -> when {
                command.size != 1 -> println("Incorrect command")
                else -> println(cont.joinToString())
            }
            "findRegex" -> when {
                command.size != 2 -> println("Incorrect command")
                else -> {
                    println(cont.filter { it.key.matches(command[1].toRegex()) }.joinToString())
                }
            }
            "clear" -> when {
                command.size != 1 -> println("Incorrect command")
                else -> {
                    cont.clear()
                    println("Done")
                }
            }
            "save" -> {
	            writeToBase(cont)
	            println("Done")
            }
            "exit" -> break
            else -> println("Incorrect command")
        }
    }
    writeToBase(cont)
}
