package com.callflow.core.model

/**
 * Additional metadata associated with a call graph node.
 * Contains Spring-specific information and behavioral markers.
 */
data class NodeMetadata(
    /** Whether the method is annotated with @Async */
    val isAsync: Boolean = false,

    /** Whether the method is annotated with @Transactional */
    val isTransactional: Boolean = false,

    /** HTTP method if this is a REST endpoint (GET, POST, etc.) */
    val httpMethod: String? = null,

    /** HTTP path mapping if this is a REST endpoint */
    val httpPath: String? = null,

    /** Event class name if this is an event publisher/listener */
    val eventClass: String? = null,

    /** All annotations on the method */
    val annotations: List<String> = emptyList(),

    /** Method visibility (public, private, protected, package-private) */
    val visibility: Visibility = Visibility.PUBLIC,

    /** Whether this method is static */
    val isStatic: Boolean = false,

    /** Parameter types for method signature display */
    val parameterTypes: List<String> = emptyList(),

    /** Return type */
    val returnType: String = "void",

    /** Line number in source file */
    val lineNumber: Int = -1
) {
    enum class Visibility {
        PUBLIC, PROTECTED, PACKAGE_PRIVATE, PRIVATE
    }

    /**
     * Returns a compact signature string like: "process(String, Long): Boolean"
     */
    fun toSignature(methodName: String): String {
        val params = parameterTypes.joinToString(", ") { it.substringAfterLast(".") }
        val ret = returnType.substringAfterLast(".")
        return "$methodName($params): $ret"
    }

    /**
     * Returns annotation badges for UI display.
     */
    fun getBadges(): List<String> = buildList {
        if (isAsync) add("@Async")
        if (isTransactional) add("@Tx")
        httpMethod?.let { add(it) }
        if (eventClass != null) add("Event")
    }
}
