package com.callflow.action

import com.callflow.core.model.CallGraph
import com.callflow.ui.toolwindow.CallFlowPanel
import com.callflow.ui.toolwindow.CallFlowToolWindowFactory
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Base class for call flow analysis actions.
 * Provides common functionality for method extraction and tool window access.
 * Supports both Java methods and Kotlin functions.
 */
abstract class BaseCallFlowAction : AnAction() {

    protected abstract val direction: CallGraph.AnalysisDirection?

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val method = getMethodAtCaret(e) ?: return

        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(CallFlowToolWindowFactory.TOOL_WINDOW_ID)

        toolWindow?.let { tw ->
            tw.show {
                val content = tw.contentManager.getContent(0)
                val panel = content?.component as? CallFlowPanel
                panel?.let {
                    direction?.let { dir -> it.setDirection(dir) }
                    it.analyzeMethod(method)
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val method = getMethodAtCaret(e)
        e.presentation.isEnabledAndVisible = method != null
    }

    protected fun getMethodAtCaret(e: AnActionEvent): PsiMethod? {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return null
        val editor = e.getData(CommonDataKeys.EDITOR)

        val offset = editor?.caretModel?.offset
            ?: e.getData(CommonDataKeys.PSI_ELEMENT)?.textOffset
            ?: return null

        val element = psiFile.findElementAt(offset)

        // Try to find Java method first
        val javaMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
        if (javaMethod != null) return javaMethod

        // Try to find Kotlin function and convert to PsiMethod
        val ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
        if (ktFunction != null) {
            // Convert Kotlin function to Light method (PsiMethod compatible)
            val lightMethods = ktFunction.toLightMethods()
            if (lightMethods.isNotEmpty()) {
                return lightMethods.first()
            }
        }

        // Fallback: check if the selected element itself is a method
        val selectedElement = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (selectedElement is PsiMethod) return selectedElement

        // Try to get Kotlin function from selected element
        if (selectedElement is KtNamedFunction) {
            val lightMethods = selectedElement.toLightMethods()
            if (lightMethods.isNotEmpty()) {
                return lightMethods.first()
            }
        }

        return null
    }
}

/**
 * Main action for analyzing call flow (bidirectional) from context menu.
 * Available on right-click in editor or project view.
 */
class AnalyzeCallFlowAction : BaseCallFlowAction() {
    override val direction: CallGraph.AnalysisDirection? = null // Use panel's current setting
}

/**
 * Action specifically for analyzing callers only.
 */
class AnalyzeCallersAction : BaseCallFlowAction() {
    override val direction = CallGraph.AnalysisDirection.CALLERS_ONLY
}

/**
 * Action specifically for analyzing callees only.
 */
class AnalyzeCalleesAction : BaseCallFlowAction() {
    override val direction = CallGraph.AnalysisDirection.CALLEES_ONLY
}
