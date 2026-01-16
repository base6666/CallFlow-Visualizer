package com.callflow.core.model

/**
 * Represents the type/role of a node in the call graph.
 * Used for visual differentiation and layer-based filtering.
 */
enum class NodeType(
    val displayName: String,
    val colorHex: String,
    val icon: String
) {
    CONTROLLER("Controller", "#4CAF50", "controller"),
    SERVICE("Service", "#2196F3", "service"),
    REPOSITORY("Repository", "#9C27B0", "repository"),
    ENTITY("Entity", "#FF6F00", "entity"),
    INTERFACE("Interface", "#607D8B", "interface"),
    IMPLEMENTATION("Implementation", "#78909C", "implementation"),
    EVENT_PUBLISHER("Event Publisher", "#FF9800", "event_pub"),
    EVENT_LISTENER("Event Listener", "#FFB74D", "event_listener"),
    COMPONENT("Component", "#00BCD4", "component"),
    CONFIGURATION("Configuration", "#795548", "config"),
    EXTERNAL("External", "#757575", "external"),
    UNKNOWN("Class", "#9E9E9E", "unknown");

    companion object {
        /**
         * Determines NodeType from class annotations.
         */
        fun fromAnnotations(annotations: List<String>): NodeType {
            return when {
                annotations.any { it.contains("Controller") || it.contains("RestController") } -> CONTROLLER
                annotations.any { it.contains("Service") } -> SERVICE
                annotations.any { it.contains("Repository") } -> REPOSITORY
                annotations.any { it.contains("Entity") } -> ENTITY
                annotations.any { it.contains("Component") } -> COMPONENT
                annotations.any { it.contains("Configuration") } -> CONFIGURATION
                annotations.any { it.contains("EventListener") } -> EVENT_LISTENER
                else -> UNKNOWN
            }
        }
    }
}
