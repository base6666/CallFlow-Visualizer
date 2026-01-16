package com.callflow.core.analyzer

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.openapi.roots.ProjectFileIndex

/**
 * Utility for resolving interface-implementation relationships.
 * Critical for accurate Spring DI call graph analysis.
 */
class InterfaceResolver(private val project: Project) {

    /**
     * Find all implementation classes of an interface or abstract class.
     *
     * @param psiClass The interface or abstract class
     * @param projectOnly If true, only return implementations in project source
     * @return List of implementing classes
     */
    fun findImplementations(psiClass: PsiClass, projectOnly: Boolean = true): List<PsiClass> {
        if (!psiClass.isInterface && !psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
            return emptyList()
        }

        val scope = GlobalSearchScope.projectScope(project)

        return ClassInheritorsSearch.search(psiClass, scope, true)
            .findAll()
            .filter { !it.isInterface } // Exclude interface extensions
            .filter { !projectOnly || isInProjectSource(it) }
            .toList()
    }

    /**
     * Find all concrete implementations of a method defined in an interface.
     *
     * @param method The interface method
     * @return List of implementing methods
     */
    fun findMethodImplementations(method: PsiMethod): List<PsiMethod> {
        val containingClass = method.containingClass ?: return emptyList()

        if (!containingClass.isInterface && !method.hasModifierProperty(PsiModifier.ABSTRACT)) {
            return emptyList()
        }

        return findImplementations(containingClass)
            .mapNotNull { implClass ->
                implClass.findMethodBySignature(method, false)
            }
    }

    /**
     * Check if a class is a Spring-managed bean.
     * Looks for @Component, @Service, @Repository, @Controller annotations.
     */
    fun isSpringBean(psiClass: PsiClass): Boolean {
        val beanAnnotations = setOf(
            "org.springframework.stereotype.Component",
            "org.springframework.stereotype.Service",
            "org.springframework.stereotype.Repository",
            "org.springframework.stereotype.Controller",
            "org.springframework.web.bind.annotation.RestController",
            "org.springframework.context.annotation.Configuration"
        )

        return psiClass.annotations.any { annotation ->
            annotation.qualifiedName in beanAnnotations
        }
    }

    /**
     * Get the primary implementation when multiple exist.
     * Uses @Primary annotation or falls back to single implementation.
     */
    fun getPrimaryImplementation(psiClass: PsiClass): PsiClass? {
        val implementations = findImplementations(psiClass)

        return when {
            implementations.isEmpty() -> null
            implementations.size == 1 -> implementations.first()
            else -> {
                // Look for @Primary annotation
                implementations.find { impl ->
                    impl.annotations.any {
                        it.qualifiedName == "org.springframework.context.annotation.Primary"
                    }
                } ?: implementations.firstOrNull { isSpringBean(it) }
            }
        }
    }

    /**
     * Resolve the actual implementation class for a field injection.
     * Handles @Autowired, @Inject, constructor injection.
     */
    fun resolveInjectedType(psiClass: PsiClass): List<PsiClass> {
        if (!psiClass.isInterface) {
            return listOf(psiClass)
        }

        val implementations = findImplementations(psiClass)
            .filter { isSpringBean(it) }

        return implementations.ifEmpty { findImplementations(psiClass) }
    }

    private fun isInProjectSource(psiClass: PsiClass): Boolean {
        val file = psiClass.containingFile?.virtualFile ?: return false
        return ProjectFileIndex.getInstance(project).isInSource(file)
    }
}
