package com.callflow.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for CallFlow Visualizer plugin.
 * Stored in IDE configuration.
 */
@State(
    name = "CallFlowSettings",
    storages = [Storage("CallFlowVisualizer.xml")]
)
class CallFlowSettings : PersistentStateComponent<CallFlowSettings.State> {

    private var myState = State()

    data class State(
        /** Default analysis depth */
        var defaultDepth: Int = 5,

        /** Maximum nodes before truncation warning */
        var maxNodes: Int = 1000,

        /** Whether to resolve interface implementations */
        var resolveImplementations: Boolean = true,

        /** Whether to track Spring Events */
        var trackSpringEvents: Boolean = true,

        /** Whether to include external library calls */
        var includeExternalCalls: Boolean = false,

        /** Packages to exclude from analysis (comma-separated) */
        var excludePackages: String = "java.,javax.,kotlin.,kotlinx.",

        /** Whether to show @Async badge */
        var showAsyncBadge: Boolean = true,

        /** Whether to show @Transactional badge */
        var showTransactionalBadge: Boolean = true,

        /** Whether to show HTTP method badge */
        var showHttpMethodBadge: Boolean = true,

        /** Default export format */
        var defaultExportFormat: String = "mermaid",

        /** Whether to auto-expand tree on analysis */
        var autoExpandTree: Boolean = true,

        /** Number of levels to auto-expand */
        var autoExpandLevels: Int = 2
    )

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    // Convenience accessors
    var defaultDepth: Int
        get() = myState.defaultDepth
        set(value) { myState.defaultDepth = value }

    var maxNodes: Int
        get() = myState.maxNodes
        set(value) { myState.maxNodes = value }

    var resolveImplementations: Boolean
        get() = myState.resolveImplementations
        set(value) { myState.resolveImplementations = value }

    var trackSpringEvents: Boolean
        get() = myState.trackSpringEvents
        set(value) { myState.trackSpringEvents = value }

    var includeExternalCalls: Boolean
        get() = myState.includeExternalCalls
        set(value) { myState.includeExternalCalls = value }

    fun getExcludePackagesList(): List<String> =
        myState.excludePackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    companion object {
        fun getInstance(): CallFlowSettings =
            ApplicationManager.getApplication().getService(CallFlowSettings::class.java)
    }
}
