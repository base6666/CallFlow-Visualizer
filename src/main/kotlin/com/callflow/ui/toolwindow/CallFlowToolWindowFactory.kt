package com.callflow.ui.toolwindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for creating the CallFlow tool window.
 * Registered in plugin.xml as the main entry point for the UI.
 */
class CallFlowToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val callFlowPanel = CallFlowPanel(project, toolWindow)
        val content = ContentFactory.getInstance().createContent(
            callFlowPanel,
            "Call Graph",
            false
        )
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true

    companion object {
        const val TOOL_WINDOW_ID = "CallFlow"
    }
}
