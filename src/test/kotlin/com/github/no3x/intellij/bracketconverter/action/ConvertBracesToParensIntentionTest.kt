package com.github.no3x.intellij.bracketconverter.action

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.idea.KotlinFileType

class ConvertBracesToParensIntentionTest : BasePlatformTestCase() {

    fun testConversions() {
        data class Case(
            val before: String,
            val intentionText: String,
            val after: String
        )

        val cases = listOf(
            Case(
                before = """
                    fun main() {
                        listOf(1, 2).map {<caret> it * 2 }
                    }
                """.trimIndent(),
                intentionText = "Convert { } to ( )",
                after = """
                    fun main() {
                        listOf(1, 2).map (<caret> it * 2 )
                    }
                """.trimIndent()
            ),
            Case(
                before = """
                    fun main() {
                        val transformers = mapOf(
                            "trim" to (<caret>s: String -> s.trim() ),
                            "upper" to (s: String -> s.uppercase())
                        )
                    }
                """.trimIndent(),
                intentionText = "Convert ( ) to { }",
                after = """
                    fun main() {
                        val transformers = mapOf(
                            "trim" to {<caret>s: String -> s.trim() },
                            "upper" to (s: String -> s.uppercase())
                        )
                    }
                """.trimIndent()
            ),
            Case(
                before = """
                    fun main() {
                        val transformers = mapOf(
                            "trim" to (<caret>s: String -> s.trim() ),
                            "upper" to (s: String -> s.uppercase())
                        )
                    }
                """.trimIndent(),
                intentionText = "Convert ( ) to { }",
                after = """
                    fun main() {
                        val transformers = mapOf(
                            "trim" to {<caret>s: String -> s.trim() },
                            "upper" to (s: String -> s.uppercase())
                        )
                    }
                """.trimIndent()
            )
        )

        cases.forEach { (before, intentionText, after) ->
            myFixture.configureByText(KotlinFileType.INSTANCE, before)
            val intention = myFixture.findSingleIntention(intentionText)
            myFixture.launchAction(intention)
            myFixture.checkResult(after)
        }
    }
}
