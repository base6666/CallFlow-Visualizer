package com.callflow.action

import com.callflow.export.MermaidExporter
import com.callflow.ui.toolwindow.CallFlowPanel
import com.callflow.ui.toolwindow.CallFlowToolWindowFactory
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import java.awt.datatransfer.StringSelection

/**
 * Action to export current call graph as Mermaid diagram.
 */
class ExportMermaidAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(CallFlowToolWindowFactory.TOOL_WINDOW_ID)

        val content = toolWindow?.contentManager?.getContent(0)
        val panel = content?.component as? CallFlowPanel
        val graph = panel?.getCurrentGraph()

        if (graph == null) {
            Messages.showWarningDialog(
                project,
                "No call graph available. Please analyze a method first.",
                "Export Error"
            )
            return
        }

        val mermaid = MermaidExporter().export(graph)

        // Copy to clipboard
        CopyPasteManager.getInstance().setContents(StringSelection(mermaid))

        Messages.showInfoMessage(
            project,
            "Mermaid diagram copied to clipboard!\n\n" +
                    "Preview (first 500 chars):\n${mermaid.take(500)}${if (mermaid.length > 500) "..." else ""}",
            "Export Successful"
        )
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabled = false
            return
        }

        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow(CallFlowToolWindowFactory.TOOL_WINDOW_ID)

        val content = toolWindow?.contentManager?.getContent(0)
        val panel = content?.component as? CallFlowPanel

        e.presentation.isEnabled = panel?.getCurrentGraph() != null
    }
}
