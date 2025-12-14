package com.github.no3x.intellij.bracketconverter.action

import com.github.no3x.intellij.bracketconverter.MyBundle
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
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

    override fun getFamilyName(): String = MyBundle.message("intentionaction.convertBracesToParentheses.familyName")

    override fun getText(): String = when (mode) {
        Mode.CURLY_TO_PAREN -> MyBundle.message("intentionaction.convertBracesToParentheses.brackets2parentheses")
        Mode.PAREN_TO_CURLY -> MyBundle.message("intentionaction.convertBracesToParentheses.parentheses2brackets")
        Mode.NONE -> ""
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
        IntentionPreviewUtils.write<RuntimeException> {
            doc.replaceString(rBrace.textRange.startOffset, rBrace.textRange.endOffset, ")")
            doc.replaceString(lBrace.textRange.startOffset, lBrace.textRange.endOffset, "(")
        }
    }

    // ---------------- ( ) → { } (unchanged, still naive) ----------------

    private fun convertParenToCurly(project: Project, editor: Editor, leaf: PsiElement) {
        val lParen = leaf
        val rParen = findMatchingRParen(lParen) ?: return

        val doc = editor.document
        IntentionPreviewUtils.write<RuntimeException> {
            doc.replaceString(rParen.textRange.startOffset, rParen.textRange.endOffset, "}")
            doc.replaceString(lParen.textRange.startOffset, lParen.textRange.endOffset, "{")
        }
    }

    private fun findMatchingRParen(lParen: PsiElement): PsiElement? {
        var depth = 0
        var current = PsiTreeUtil.nextLeaf(lParen)
        while (current != null) {
            val type = current.node.elementType
            when (type) {
                KtTokens.LPAR -> depth++
                KtTokens.RPAR -> if (depth == 0) return current else depth--
            }
            current = PsiTreeUtil.nextLeaf(current)
        }
        return null
    }
}
