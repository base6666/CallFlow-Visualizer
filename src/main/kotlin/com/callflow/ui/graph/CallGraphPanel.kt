package com.callflow.ui.graph

import com.callflow.core.model.CallNode
import com.callflow.core.model.NodeType
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.*
import java.awt.geom.*
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.max
import kotlin.math.min

/**
 * Visual graph panel that renders call flow as an interactive diagram.
 * Supports zoom, pan, and node selection.
 */
class CallGraphPanel : JPanel() {

    private var rootNode: CallNode? = null
    private var graphNodes: MutableList<GraphNode> = mutableListOf()
    private var graphEdges: MutableList<GraphEdge> = mutableListOf()

    // View transformation
    private var scale: Double = 1.0
    private var offsetX: Double = 50.0
    private var offsetY: Double = 50.0

    // Interaction state
    private var dragStartPoint: Point? = null
    private var lastDragPoint: Point? = null
    private var selectedNode: GraphNode? = null
    private var hoveredNode: GraphNode? = null

    // Layout constants - Horizontal layout (left to right)
    private val nodeWidth = 200
    private val nodeHeight = 60
    private val horizontalGap = 60
    private val verticalGap = 30

    // Zoom limits
    private val minScale = 0.2
    private val maxScale = 3.0

    init {
        background = JBColor.background()
        isDoubleBuffered = true

        // Mouse wheel for zoom
        addMouseWheelListener { e ->
            val oldScale = scale
            val zoomFactor = if (e.wheelRotation < 0) 1.1 else 0.9
            scale = (scale * zoomFactor).coerceIn(minScale, maxScale)

            // Zoom towards mouse position
            val mouseX = e.x
            val mouseY = e.y
            offsetX = mouseX - (mouseX - offsetX) * (scale / oldScale)
            offsetY = mouseY - (mouseY - offsetY) * (scale / oldScale)

            repaint()
        }

        // Mouse drag for panning
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    val clickedNode = findNodeAt(e.point)
                    if (clickedNode != null) {
                        selectedNode = clickedNode
                        // Navigate to source on single click
                        clickedNode.callNode.navigate(true)
                    } else {
                        dragStartPoint = e.point
                        lastDragPoint = e.point
                    }
                    repaint()
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                dragStartPoint = null
                lastDragPoint = null
                cursor = Cursor.getDefaultCursor()
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (dragStartPoint != null && lastDragPoint != null) {
                    cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                    val dx = e.x - lastDragPoint!!.x
                    val dy = e.y - lastDragPoint!!.y
                    offsetX += dx
                    offsetY += dy
                    lastDragPoint = e.point
                    repaint()
                }
            }

            override fun mouseMoved(e: MouseEvent) {
                val node = findNodeAt(e.point)
                if (node != hoveredNode) {
                    hoveredNode = node
                    cursor = if (node != null) {
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    } else {
                        Cursor.getDefaultCursor()
                    }
                    repaint()
                }
            }
        })
    }

    /**
     * Set the root call node and rebuild the graph layout.
     */
    fun setRoot(callNode: CallNode) {
        this.rootNode = callNode
        buildGraphLayout()

        // Center the view on the root node
        SwingUtilities.invokeLater {
            centerOnRoot()
            repaint()
        }
    }

    /**
     * Reset zoom to 100%.
     */
    fun resetZoom() {
        scale = 1.0
        centerOnRoot()
        repaint()
    }

    /**
     * Zoom in by 20%.
     */
    fun zoomIn() {
        val centerX = width / 2.0
        val centerY = height / 2.0
        val oldScale = scale
        scale = (scale * 1.2).coerceAtMost(maxScale)
        offsetX = centerX - (centerX - offsetX) * (scale / oldScale)
        offsetY = centerY - (centerY - offsetY) * (scale / oldScale)
        repaint()
    }

    /**
     * Zoom out by 20%.
     */
    fun zoomOut() {
        val centerX = width / 2.0
        val centerY = height / 2.0
        val oldScale = scale
        scale = (scale / 1.2).coerceAtLeast(minScale)
        offsetX = centerX - (centerX - offsetX) * (scale / oldScale)
        offsetY = centerY - (centerY - offsetY) * (scale / oldScale)
        repaint()
    }

    /**
     * Fit the entire graph in view.
     */
    fun fitToView() {
        if (graphNodes.isEmpty()) return

        val minX = graphNodes.minOf { it.x }
        val maxX = graphNodes.maxOf { it.x + nodeWidth }
        val minY = graphNodes.minOf { it.y }
        val maxY = graphNodes.maxOf { it.y + nodeHeight }

        val graphWidth = maxX - minX + 100
        val graphHeight = maxY - minY + 100

        val scaleX = width.toDouble() / graphWidth
        val scaleY = height.toDouble() / graphHeight
        scale = min(scaleX, scaleY).coerceIn(minScale, maxScale)

        offsetX = (width - graphWidth * scale) / 2 - minX * scale + 50
        offsetY = (height - graphHeight * scale) / 2 - minY * scale + 50

        repaint()
    }

    private fun centerOnRoot() {
        val rootGraphNode = graphNodes.firstOrNull() ?: return
        offsetX = width / 2.0 - (rootGraphNode.x + nodeWidth / 2) * scale
        offsetY = 80.0
    }

    private fun buildGraphLayout() {
        graphNodes.clear()
        graphEdges.clear()

        val root = rootNode ?: return

        // Map to track unique nodes by their ID (prevents duplicates)
        val nodeMap = mutableMapOf<String, GraphNode>()
        val edgeSet = mutableSetOf<String>()

        // Collect unique nodes by depth level
        val callersByDepth = mutableMapOf<Int, MutableSet<String>>()
        val calleesByDepth = mutableMapOf<Int, MutableSet<String>>()
        val allNodes = mutableMapOf<String, CallNode>()

        // First pass: collect all unique nodes
        collectUniqueNodes(root.callers, callersByDepth, allNodes, 1, isCallers = true)
        collectUniqueNodes(root.callees, calleesByDepth, allNodes, 1, isCallers = false)

        // Calculate max nodes per level
        val maxCallerLevel = callersByDepth.values.maxOfOrNull { it.size } ?: 0
        val maxCalleeLevel = calleesByDepth.values.maxOfOrNull { it.size } ?: 0
        val maxLevel = max(maxCallerLevel, maxCalleeLevel)
        val totalHeight = max(1, maxLevel) * (nodeHeight + verticalGap)

        // Create root node
        val rootY = totalHeight / 2 - nodeHeight / 2
        val rootGraphNode = GraphNode(root, 0, rootY)
        graphNodes.add(rootGraphNode)
        nodeMap[root.id] = rootGraphNode

        // Layout caller nodes (LEFT)
        callersByDepth.keys.sorted().forEach { depth ->
            val nodeIds = callersByDepth[depth]!!.toList()
            layoutNodesAtDepth(nodeIds, allNodes, nodeMap, depth, isCallers = true, totalHeight)
        }

        // Layout callee nodes (RIGHT)
        calleesByDepth.keys.sorted().forEach { depth ->
            val nodeIds = calleesByDepth[depth]!!.toList()
            layoutNodesAtDepth(nodeIds, allNodes, nodeMap, depth, isCallers = false, totalHeight)
        }

        // Build edges
        buildAllEdges(root, nodeMap, edgeSet, isCallers = true)
        buildAllEdges(root, nodeMap, edgeSet, isCallers = false)
    }

    /**
     * Collect unique nodes recursively.
     */
    private fun collectUniqueNodes(
        nodes: List<CallNode>,
        byDepth: MutableMap<Int, MutableSet<String>>,
        allNodes: MutableMap<String, CallNode>,
        depth: Int,
        isCallers: Boolean
    ) {
        if (depth > 5) return

        nodes.forEach { node ->
            val nodeId = node.id
            if (nodeId !in allNodes) {
                allNodes[nodeId] = node
                byDepth.getOrPut(depth) { mutableSetOf() }.add(nodeId)
            }

            if (!node.isCyclicRef) {
                val children = if (isCallers) node.callers else node.callees
                collectUniqueNodes(children, byDepth, allNodes, depth + 1, isCallers)
            }
        }
    }

    /**
     * Layout nodes at a specific depth level.
     */
    private fun layoutNodesAtDepth(
        nodeIds: List<String>,
        allNodes: Map<String, CallNode>,
        nodeMap: MutableMap<String, GraphNode>,
        depth: Int,
        isCallers: Boolean,
        totalHeight: Int
    ) {
        val count = nodeIds.size
        val levelHeight = count * (nodeHeight + verticalGap) - verticalGap
        var y = totalHeight / 2 - levelHeight / 2

        val x = if (isCallers) {
            -(nodeWidth + horizontalGap) * depth
        } else {
            (nodeWidth + horizontalGap) * depth
        }

        nodeIds.forEach { nodeId ->
            if (nodeId !in nodeMap) {
                val callNode = allNodes[nodeId]!!
                val graphNode = GraphNode(callNode, x, y)
                graphNodes.add(graphNode)
                nodeMap[nodeId] = graphNode
            }
            y += nodeHeight + verticalGap
        }
    }

    /**
     * Build edges recursively.
     */
    private fun buildAllEdges(
        node: CallNode,
        nodeMap: Map<String, GraphNode>,
        edgeSet: MutableSet<String>,
        isCallers: Boolean
    ) {
        val graphNode = nodeMap[node.id] ?: return
        val children = if (isCallers) node.callers else node.callees

        children.forEach { child ->
            val childGraphNode = nodeMap[child.id] ?: return@forEach

            val edgeKey = if (isCallers) {
                "${child.id}->${node.id}"
            } else {
                "${node.id}->${child.id}"
            }

            if (edgeKey !in edgeSet) {
                edgeSet.add(edgeKey)
                val edge = if (isCallers) {
                    GraphEdge(childGraphNode, graphNode)
                } else {
                    GraphEdge(graphNode, childGraphNode)
                }
                graphEdges.add(edge)
            }

            if (!child.isCyclicRef) {
                buildAllEdges(child, nodeMap, edgeSet, isCallers)
            }
        }
    }

    private fun findNodeAt(point: Point): GraphNode? {
        val worldX = (point.x - offsetX) / scale
        val worldY = (point.y - offsetY) / scale

        return graphNodes.find { node ->
            worldX >= node.x && worldX <= node.x + nodeWidth &&
            worldY >= node.y && worldY <= node.y + nodeHeight
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D

        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        // Apply transformation
        val originalTransform = g2.transform
        g2.translate(offsetX, offsetY)
        g2.scale(scale, scale)

        // Draw edges first (behind nodes)
        graphEdges.forEach { edge ->
            drawEdge(g2, edge)
        }

        // Draw nodes
        graphNodes.forEach { node ->
            drawNode(g2, node)
        }

        // Restore transform
        g2.transform = originalTransform

        // Draw zoom indicator
        drawZoomIndicator(g2)

        // Draw instructions if no graph
        if (graphNodes.isEmpty()) {
            drawPlaceholder(g2)
        }
    }

    private fun drawEdge(g2: Graphics2D, edge: GraphEdge) {
        // Horizontal layout: edges go from right side of 'from' node to left side of 'to' node
        val fromX = edge.from.x + nodeWidth
        val fromY = edge.from.y + nodeHeight / 2
        val toX = edge.to.x
        val toY = edge.to.y + nodeHeight / 2

        // Draw curved line
        g2.color = JBColor.GRAY
        g2.stroke = BasicStroke(2f)

        val path = Path2D.Double()
        path.moveTo(fromX.toDouble(), fromY.toDouble())

        // Bezier curve for horizontal smoother edges
        val midX = (fromX + toX) / 2.0
        path.curveTo(
            midX, fromY.toDouble(),
            midX, toY.toDouble(),
            toX.toDouble(), toY.toDouble()
        )
        g2.draw(path)

        // Draw arrow head (pointing right)
        drawArrowHead(g2, toX, toY)
    }

    private fun drawArrowHead(g2: Graphics2D, x: Int, y: Int) {
        val arrowSize = 10
        val path = Path2D.Double()
        // Arrow pointing right
        path.moveTo(x.toDouble(), y.toDouble())
        path.lineTo((x - arrowSize * 1.5).toDouble(), (y - arrowSize).toDouble())
        path.lineTo((x - arrowSize * 1.5).toDouble(), (y + arrowSize).toDouble())
        path.closePath()
        g2.fill(path)
    }

    private fun drawNode(g2: Graphics2D, node: GraphNode) {
        val callNode = node.callNode
        val x = node.x
        val y = node.y

        // Check if this is the root/starting node
        val isRootNode = callNode == rootNode

        // Node background color based on type
        val bgColor = if (isRootNode) {
            Color(0xFFEBEE) // Light red background for root node
        } else {
            getBackgroundColor(callNode.type)
        }
        val isSelected = node == selectedNode
        val isHovered = node == hoveredNode

        // Draw shadow (larger for root node)
        g2.color = if (isRootNode) Color(200, 0, 0, 40) else Color(0, 0, 0, 30)
        g2.fillRoundRect(x + 3, y + 3, nodeWidth, nodeHeight, 10, 10)

        // Draw node background
        g2.color = if (isSelected) bgColor.brighter() else bgColor
        g2.fillRoundRect(x, y, nodeWidth, nodeHeight, 10, 10)

        // Draw border - RED for root node
        g2.color = when {
            isRootNode -> Color(0xE53935) // Red border for starting method
            isSelected || isHovered -> JBColor.namedColor("Focus.borderColor", JBColor.BLUE)
            else -> bgColor.darker()
        }
        g2.stroke = BasicStroke(if (isRootNode) 4f else if (isSelected || isHovered) 3f else 1.5f)
        g2.drawRoundRect(x, y, nodeWidth, nodeHeight, 10, 10)

        // Draw type badge
        val badgeColor = getBadgeColor(callNode.type)
        g2.color = badgeColor
        g2.fillRoundRect(x + 5, y + 5, 80, 20, 6, 6)
        g2.color = Color.WHITE
        g2.font = Font("SansSerif", Font.BOLD, 11)
        g2.drawString(callNode.type.displayName, x + 10, y + 19)

        // Draw class name
        g2.color = JBColor.foreground()
        g2.font = Font("SansSerif", Font.BOLD, 14)
        val className = truncateText(g2, callNode.className, nodeWidth - 15)
        g2.drawString(className, x + 8, y + 40)

        // Draw method name
        g2.color = JBColor.GRAY
        g2.font = Font("SansSerif", Font.PLAIN, 12)
        val methodName = truncateText(g2, ".${callNode.methodName}()", nodeWidth - 15)
        g2.drawString(methodName, x + 8, y + 55)

        // Draw cycle indicator
        if (callNode.isCyclicRef) {
            g2.color = JBColor.ORANGE
            g2.font = Font("SansSerif", Font.BOLD, 12)
            g2.drawString("\u21BB", x + nodeWidth - 20, y + 19)
        }

        // Draw START indicator for root node
        if (isRootNode) {
            g2.color = Color(0xE53935) // Red
            g2.font = Font("SansSerif", Font.BOLD, 11)
            g2.drawString("★ START", x + nodeWidth - 70, y + 19)
        }
    }

    private fun truncateText(g2: Graphics2D, text: String, maxWidth: Int): String {
        val fm = g2.fontMetrics
        if (fm.stringWidth(text) <= maxWidth) return text

        var truncated = text
        while (truncated.isNotEmpty() && fm.stringWidth("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated..."
    }

    private fun getBackgroundColor(type: NodeType): Color {
        return when (type) {
            NodeType.CONTROLLER -> Color(0xE3F2FD) // Light blue
            NodeType.SERVICE -> Color(0xE8F5E9) // Light green
            NodeType.REPOSITORY -> Color(0xFFF3E0) // Light orange
            NodeType.ENTITY -> Color(0xF3E5F5) // Light purple
            NodeType.INTERFACE -> Color(0xE0F7FA) // Light cyan
            NodeType.IMPLEMENTATION -> Color(0xFCE4EC) // Light pink
            NodeType.EVENT_PUBLISHER -> Color(0xFFFDE7) // Light yellow
            NodeType.EVENT_LISTENER -> Color(0xFFF8E1) // Light amber
            NodeType.COMPONENT -> Color(0xE8EAF6) // Light indigo
            NodeType.CONFIGURATION -> Color(0xEFEBE9) // Light brown
            NodeType.EXTERNAL -> Color(0xECEFF1) // Light blue grey
            NodeType.UNKNOWN -> Color(0xFAFAFA) // Light grey
        }
    }

    private fun getBadgeColor(type: NodeType): Color {
        return try {
            Color.decode(type.colorHex)
        } catch (e: Exception) {
            Color.GRAY
        }
    }

    private fun drawZoomIndicator(g2: Graphics2D) {
        g2.color = JBColor.foreground()
        g2.font = Font("SansSerif", Font.PLAIN, 11)
        val zoomText = "Zoom: ${(scale * 100).toInt()}%"
        g2.drawString(zoomText, 10, height - 10)

        // Draw controls hint
        g2.color = JBColor.GRAY
        g2.font = Font("SansSerif", Font.PLAIN, 10)
        g2.drawString("Scroll: Zoom | Drag: Pan | Double-click: Navigate", 10, height - 25)
    }

    private fun drawPlaceholder(g2: Graphics2D) {
        g2.color = JBColor.GRAY
        g2.font = Font("SansSerif", Font.PLAIN, 14)
        val message = "No call graph. Right-click a method and select 'Analyze Call Flow'"
        val fm = g2.fontMetrics
        val x = (width - fm.stringWidth(message)) / 2
        val y = height / 2
        g2.drawString(message, x, y)
    }

    /**
     * Internal class representing a visual node in the graph.
     */
    data class GraphNode(
        val callNode: CallNode,
        var x: Int,
        var y: Int
    )

    /**
     * Internal class representing an edge between two nodes.
     */
    data class GraphEdge(
        val from: GraphNode,
        val to: GraphNode
    )
}
