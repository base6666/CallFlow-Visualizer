package com.callflow.core.analyzer

import com.callflow.core.model.CallGraph
import com.callflow.core.model.CallNode
import com.intellij.psi.PsiMethod

/**
 * Interface for analyzing method call relationships.
 * Implementations should be language-specific (Java, Kotlin, etc.)
 */
interface CallAnalyzer {

    /**
     * Analyze callee methods (methods called by the given method).
     *
     * @param method The starting method
     * @param depth Maximum depth to traverse
     * @return Root CallNode with callees populated
     */
    fun analyzeCallees(method: PsiMethod, depth: Int): CallNode

    /**
     * Analyze caller methods (methods that call the given method).
     *
     * @param method The starting method
     * @param depth Maximum depth to traverse
     * @return Root CallNode with callers populated
     */
    fun analyzeCallers(method: PsiMethod, depth: Int): CallNode

    /**
     * Analyze both directions and return a complete CallGraph.
     *
     * @param method The starting method
     * @param depth Maximum depth for each direction
     * @return Complete CallGraph with bidirectional analysis
     */
    fun analyzeBidirectional(method: PsiMethod, depth: Int): CallGraph
}

/**
 * Configuration for call analysis behavior.
 */
data class AnalysisConfig(
    /** Maximum depth for traversal */
    val maxDepth: Int = 5,

    /** Whether to resolve interface implementations */
    val resolveImplementations: Boolean = true,

    /** Whether to track Spring Events */
    val trackSpringEvents: Boolean = true,

    /** Whether to include external library calls */
    val includeExternalCalls: Boolean = false,

    /** Package filters (empty = include all) */
    val includePackages: List<String> = emptyList(),

    /** Package exclusions */
    val excludePackages: List<String> = listOf(
        "java.",
        "javax.",
        "kotlin.",
        "kotlinx.",
        "org.springframework.boot.",
        "org.springframework.context.annotation."
    ),

    /** Maximum nodes to prevent performance issues */
    val maxNodes: Int = 1000
)
