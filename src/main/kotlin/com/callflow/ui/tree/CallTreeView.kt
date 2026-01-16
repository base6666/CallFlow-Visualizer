package com.callflow.ui.tree

import com.callflow.core.model.CallNode
import com.callflow.core.model.NodeType
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

/**
 * Tree view component for displaying the call graph hierarchy.
 * Supports both caller (upstream) and callee (downstream) display.
 */
class CallTreeView(private val project: Project) : Tree() {

    private var myRootNode = DefaultMutableTreeNode("No analysis yet")

    init {
        model = DefaultTreeModel(myRootNode)
        isRootVisible = true
        showsRootHandles = true
        cellRenderer = CallTreeCellRenderer()

        // Double-click to navigate
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    navigateToSelected()
                }
            }
        })

        // Enter key to navigate
        addKeyListener(object : java.awt.event.KeyAdapter() {
            override fun keyPressed(e: java.awt.event.KeyEvent) {
                if (e.keyCode == java.awt.event.KeyEvent.VK_ENTER) {
                    navigateToSelected()
                }
            }
        })
    }

    /**
     * Set the root CallNode and rebuild the tree.
     */
    fun setRoot(callNode: CallNode) {
        // Create new root node with the CallNode
        myRootNode = DefaultMutableTreeNode(callNode)

        // Add callers (upstream) - shown above or in separate section
        if (callNode.callers.isNotEmpty()) {
            val callersSection = DefaultMutableTreeNode(SectionHeader("Callers (${callNode.callers.size})", true))
            callNode.callers.forEach { caller ->
                addNodeRecursively(callersSection, caller, isCallerDirection = true)
            }
            myRootNode.add(callersSection)
        }

        // Add callees (downstream) - shown below or in separate section
        if (callNode.callees.isNotEmpty()) {
            val calleesSection = DefaultMutableTreeNode(SectionHeader("Callees (${callNode.callees.size})", false))
            callNode.callees.forEach { callee ->
                addNodeRecursively(calleesSection, callee, isCallerDirection = false)
            }
            myRootNode.add(calleesSection)
        }

        // Create new tree model and set it
        val newModel = DefaultTreeModel(myRootNode)
        setModel(newModel)

        // Re-apply cell renderer after model change
        setCellRenderer(CallTreeCellRenderer())

        // Use SwingUtilities to ensure UI updates on EDT
        javax.swing.SwingUtilities.invokeLater {
            // Expand root row
            expandRow(0)

            // Expand first level (callers/callees sections)
            for (i in 0 until myRootNode.childCount) {
                expandRow(i + 1)
            }

            // Force repaint
            revalidate()
            repaint()
        }
    }

    private fun addNodeRecursively(
        parent: DefaultMutableTreeNode,
        node: CallNode,
        isCallerDirection: Boolean
    ) {
        val treeNode = DefaultMutableTreeNode(node)
        parent.add(treeNode)

        val children = if (isCallerDirection) node.callers else node.callees
        children.forEach { child ->
            addNodeRecursively(treeNode, child, isCallerDirection)
        }
    }

    private fun navigateToSelected() {
        val selectedNode = lastSelectedPathComponent as? DefaultMutableTreeNode ?: return
        val callNode = selectedNode.userObject as? CallNode ?: return
        callNode.navigate(true)
    }

    /**
     * Section header marker class.
     */
    data class SectionHeader(val title: String, val isCallers: Boolean)
}

/**
 * Custom cell renderer for CallNode display.
 */
class CallTreeCellRenderer : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        val node = (value as? DefaultMutableTreeNode)?.userObject

        when (node) {
            is CallNode -> renderCallNode(node)
            is CallTreeView.SectionHeader -> renderSectionHeader(node)
            is String -> {
                append(node, SimpleTextAttributes.GRAYED_ATTRIBUTES)
                icon = AllIcons.Nodes.EmptyNode
            }
        }
    }

    private fun renderCallNode(node: CallNode) {
        // Icon based on type
        icon = getIconForType(node.type)

        // Class name
        append(node.className, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)

        // Method name
        append(".${node.methodName}()", SimpleTextAttributes.REGULAR_ATTRIBUTES)

        // Badges (Async, Transactional, HTTP method, etc.)
        val badges = node.metadata.getBadges()
        if (badges.isNotEmpty()) {
            append(" ")
            badges.forEach { badge ->
                append("[$badge]", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
                append(" ")
            }
        }

        // Cyclic reference indicator
        if (node.isCyclicRef) {
            append(" [↻ cycle]", SimpleTextAttributes(
                SimpleTextAttributes.STYLE_ITALIC,
                Color.ORANGE
            ))
        }

        // Type badge with color
        val typeColor = getColorForType(node.type)
        if (node.type != NodeType.UNKNOWN) {
            append(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
            append(node.type.displayName, SimpleTextAttributes(
                SimpleTextAttributes.STYLE_SMALLER,
                typeColor
            ))
        }

        toolTipText = buildString {
            append("<html>")
            append("<b>${escapeHtml(node.qualifiedClassName)}.${escapeHtml(node.methodName)}</b><br/>")
            append("Type: ${escapeHtml(node.type.displayName)}<br/>")
            if (node.metadata.annotations.isNotEmpty()) {
                append("Annotations: ${escapeHtml(node.metadata.annotations.joinToString(", "))}<br/>")
            }
            append("Signature: ${escapeHtml(node.metadata.toSignature(node.methodName))}")
            append("</html>")
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun renderSectionHeader(header: CallTreeView.SectionHeader) {
        icon = if (header.isCallers) {
            AllIcons.Hierarchy.Supertypes
        } else {
            AllIcons.Hierarchy.Subtypes
        }
        append(header.title, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    }

    private fun getIconForType(type: NodeType): Icon {
        return when (type) {
            NodeType.CONTROLLER -> AllIcons.Nodes.Controller
            NodeType.SERVICE -> AllIcons.Nodes.Plugin
            NodeType.REPOSITORY -> AllIcons.Nodes.DataTables
            NodeType.ENTITY -> AllIcons.Nodes.Class
            NodeType.INTERFACE -> AllIcons.Nodes.Interface
            NodeType.IMPLEMENTATION -> AllIcons.Nodes.Class
            NodeType.EVENT_PUBLISHER -> AllIcons.Actions.Lightning
            NodeType.EVENT_LISTENER -> AllIcons.Nodes.EntryPoints
            NodeType.COMPONENT -> AllIcons.Nodes.Artifact
            NodeType.CONFIGURATION -> AllIcons.Nodes.Property
            NodeType.EXTERNAL -> AllIcons.Nodes.PpLib
            NodeType.UNKNOWN -> AllIcons.Nodes.Method
        }
    }

    private fun getColorForType(type: NodeType): Color {
        return try {
            Color.decode(type.colorHex)
        } catch (e: Exception) {
            JBUI.CurrentTheme.Label.foreground()
        }
    }
}
