package com.example.util

object ProfanityFilter {
    private val badWords = listOf(
        "küfür", "aptal", "salak", "gerizekalı", "ahmak", "moron", "piç", "lanet", "bok", "yavşak", "it", "köpek", "mal"
    )

    fun filter(text: String, isEnabled: Boolean = true): String {
        if (!isEnabled || text.isBlank()) return text
        var result = text
        for (word in badWords) {
            val regex = Regex("(?i)\\b$word\\b")
            result = result.replace(regex, "*".repeat(word.length))
        }
        return result
    }
}
