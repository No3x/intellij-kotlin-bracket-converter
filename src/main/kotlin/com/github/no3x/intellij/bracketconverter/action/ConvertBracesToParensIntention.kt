package com.github.no3x.intellij.bracketconverter.action

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFunctionLiteral

class ConvertBracesToParensIntention : PsiElementBaseIntentionAction() {

    private enum class Mode { CURLY_TO_PAREN, PAREN_TO_CURLY, NONE }
    private var mode: Mode = Mode.NONE

    override fun getFamilyName(): String = "Kotlin Bracket Conversion"

    override fun getText(): String = when (mode) {
        Mode.CURLY_TO_PAREN -> "Convert { } to ( )"
        Mode.PAREN_TO_CURLY -> "Convert ( ) to { }"
        Mode.NONE -> "Convert brackets"
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (editor == null) return false
        val file = element.containingFile ?: return false

        val bracket = findBracketElement(file, editor.caretModel.offset) ?: return false
        mode = detectMode(bracket)

        return mode != Mode.NONE
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        val file = element.containingFile ?: return
        val leaf = findBracketElement(file, editor.caretModel.offset) ?: return

        when (mode) {
            Mode.CURLY_TO_PAREN -> convertCurlyLambdaToParen(project, editor, leaf)
            Mode.PAREN_TO_CURLY -> convertParenToCurly(project, editor, leaf)
            Mode.NONE -> {}
        }
    }

    override fun startInWriteAction(): Boolean = true

    // ---------------- detection ----------------

    private fun findBracketElement(file: PsiFile, caretOffset: Int): PsiElement? {
        val leafAt = file.findElementAt(caretOffset)
        if (leafAt != null && isBracketToken(leafAt)) return leafAt
        if (caretOffset > 0) {
            val prev = file.findElementAt(caretOffset - 1)
            if (prev != null && isBracketToken(prev)) return prev
        }
        return null
    }

    private fun isBracketToken(element: PsiElement): Boolean {
        val tokenType = element.node.elementType
        return tokenType == KtTokens.LPAR || tokenType == KtTokens.LBRACE
    }

    private fun detectMode(leaf: PsiElement): Mode {
        val tokenType = leaf.node.elementType

        // LAMBDA_EXPRESSION -> FUNCTION_LITERAL -> LBRACE
        if (tokenType == KtTokens.LBRACE && findLambdaFunctionLiteral(leaf) != null) {
            return Mode.CURLY_TO_PAREN
        }

        // Simple: caret on '(' → convert to '{ }'
        if (tokenType == KtTokens.LPAR) {
            return Mode.PAREN_TO_CURLY
        }

        return Mode.NONE
    }

    private fun findLambdaFunctionLiteral(leaf: PsiElement): KtFunctionLiteral? {
        // we know leaf is LBRACE, walk up to FUNCTION_LITERAL
        return PsiTreeUtil.getParentOfType(leaf, KtFunctionLiteral::class.java, false)
    }

    // ---------------- { } → ( ) for lambdas ----------------

    private fun convertCurlyLambdaToParen(project: Project, editor: Editor, leaf: PsiElement) {
        val functionLiteral = findLambdaFunctionLiteral(leaf) ?: return
        val lBrace = functionLiteral.lBrace ?: return
        val rBrace = functionLiteral.rBrace ?: return

        val doc = editor.document
        doc.replaceString(rBrace.textRange.startOffset, rBrace.textRange.endOffset, ")")
        doc.replaceString(lBrace.textRange.startOffset, lBrace.textRange.endOffset, "(")
    }

    // ---------------- ( ) → { } (unchanged, still naive) ----------------

    private fun convertParenToCurly(project: Project, editor: Editor, leaf: PsiElement) {
        val lParen = leaf
        val doc = editor.document
        val text = doc.charsSequence

        // Find the matching right paren for this left paren, skipping nested parens.
        var depth = 0
        var rOffset = -1
        for (i in lParen.textRange.endOffset until text.length) {
            when (text[i]) {
                '(' -> depth++
                ')' -> if (depth == 0) {
                    rOffset = i
                    break
                } else {
                    depth--
                }
            }
        }
        if (rOffset == -1) return
        doc.replaceString(rOffset, rOffset + 1, "}")
        doc.replaceString(lParen.textRange.startOffset, lParen.textRange.endOffset, "{")
    }
}
