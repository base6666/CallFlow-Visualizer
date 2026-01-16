package com.callflow.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * Settings UI panel for CallFlow Visualizer.
 * Accessible via Settings → Tools → CallFlow Visualizer.
 */
class CallFlowConfigurable : Configurable {

    private var mainPanel: JPanel? = null

    // UI Components
    private val depthSpinner = JSpinner(SpinnerNumberModel(5, 1, 20, 1))
    private val maxNodesSpinner = JSpinner(SpinnerNumberModel(1000, 100, 10000, 100))
    private val resolveImplCheckbox = JBCheckBox("Resolve interface implementations")
    private val trackEventsCheckbox = JBCheckBox("Track Spring Events")
    private val includeExternalCheckbox = JBCheckBox("Include external library calls")
    private val excludePackagesField = JBTextField()
    private val showAsyncBadgeCheckbox = JBCheckBox("Show @Async badge")
    private val showTxBadgeCheckbox = JBCheckBox("Show @Transactional badge")
    private val showHttpBadgeCheckbox = JBCheckBox("Show HTTP method badge")
    private val autoExpandCheckbox = JBCheckBox("Auto-expand tree on analysis")
    private val autoExpandLevelsSpinner = JSpinner(SpinnerNumberModel(2, 1, 10, 1))

    override fun getDisplayName(): String = "CallFlow Visualizer"

    override fun createComponent(): JComponent {
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Default analysis depth:"), depthSpinner)
            .addLabeledComponent(JBLabel("Maximum nodes:"), maxNodesSpinner)
            .addSeparator()
            .addComponent(JBLabel("Analysis Options"))
            .addComponent(resolveImplCheckbox)
            .addComponent(trackEventsCheckbox)
            .addComponent(includeExternalCheckbox)
            .addLabeledComponent(
                JBLabel("Exclude packages (comma-separated):"),
                excludePackagesField
            )
            .addSeparator()
            .addComponent(JBLabel("Display Options"))
            .addComponent(showAsyncBadgeCheckbox)
            .addComponent(showTxBadgeCheckbox)
            .addComponent(showHttpBadgeCheckbox)
            .addComponent(autoExpandCheckbox)
            .addLabeledComponent(JBLabel("Auto-expand levels:"), autoExpandLevelsSpinner)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = CallFlowSettings.getInstance()
        return depthSpinner.value != settings.myState.defaultDepth ||
                maxNodesSpinner.value != settings.myState.maxNodes ||
                resolveImplCheckbox.isSelected != settings.myState.resolveImplementations ||
                trackEventsCheckbox.isSelected != settings.myState.trackSpringEvents ||
                includeExternalCheckbox.isSelected != settings.myState.includeExternalCalls ||
                excludePackagesField.text != settings.myState.excludePackages ||
                showAsyncBadgeCheckbox.isSelected != settings.myState.showAsyncBadge ||
                showTxBadgeCheckbox.isSelected != settings.myState.showTransactionalBadge ||
                showHttpBadgeCheckbox.isSelected != settings.myState.showHttpMethodBadge ||
                autoExpandCheckbox.isSelected != settings.myState.autoExpandTree ||
                autoExpandLevelsSpinner.value != settings.myState.autoExpandLevels
    }

    override fun apply() {
        val settings = CallFlowSettings.getInstance()
        settings.myState.defaultDepth = depthSpinner.value as Int
        settings.myState.maxNodes = maxNodesSpinner.value as Int
        settings.myState.resolveImplementations = resolveImplCheckbox.isSelected
        settings.myState.trackSpringEvents = trackEventsCheckbox.isSelected
        settings.myState.includeExternalCalls = includeExternalCheckbox.isSelected
        settings.myState.excludePackages = excludePackagesField.text
        settings.myState.showAsyncBadge = showAsyncBadgeCheckbox.isSelected
        settings.myState.showTransactionalBadge = showTxBadgeCheckbox.isSelected
        settings.myState.showHttpMethodBadge = showHttpBadgeCheckbox.isSelected
        settings.myState.autoExpandTree = autoExpandCheckbox.isSelected
        settings.myState.autoExpandLevels = autoExpandLevelsSpinner.value as Int
    }

    override fun reset() {
        val settings = CallFlowSettings.getInstance()
        depthSpinner.value = settings.myState.defaultDepth
        maxNodesSpinner.value = settings.myState.maxNodes
        resolveImplCheckbox.isSelected = settings.myState.resolveImplementations
        trackEventsCheckbox.isSelected = settings.myState.trackSpringEvents
        includeExternalCheckbox.isSelected = settings.myState.includeExternalCalls
        excludePackagesField.text = settings.myState.excludePackages
        showAsyncBadgeCheckbox.isSelected = settings.myState.showAsyncBadge
        showTxBadgeCheckbox.isSelected = settings.myState.showTransactionalBadge
        showHttpBadgeCheckbox.isSelected = settings.myState.showHttpMethodBadge
        autoExpandCheckbox.isSelected = settings.myState.autoExpandTree
        autoExpandLevelsSpinner.value = settings.myState.autoExpandLevels
    }

    override fun disposeUIResources() {
        mainPanel = null
    }

    // Make myState accessible for isModified check
    private val CallFlowSettings.myState: CallFlowSettings.State
        get() = this.state ?: CallFlowSettings.State()
}
