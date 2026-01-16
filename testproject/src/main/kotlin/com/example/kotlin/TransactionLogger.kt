package com.example.kotlin

import org.springframework.stereotype.Component

/**
 * Kotlin transaction logging service
 */
@Component
class TransactionLogger(
    private val auditService: AuditService
) {
    fun logStart(orderId: String, amount: Double) {
        auditService.record(
            AuditEvent(
                type = "PAYMENT_START",
                orderId = orderId,
                details = "Payment initiated for amount: $amount"
            )
        )
    }

    fun logSuccess(orderId: String, transactionId: String?) {
        auditService.record(
            AuditEvent(
                type = "PAYMENT_SUCCESS",
                orderId = orderId,
                details = "Transaction completed: $transactionId"
            )
        )
    }

    fun logFailure(orderId: String, errorMessage: String?) {
        auditService.record(
            AuditEvent(
                type = "PAYMENT_FAILURE",
                orderId = orderId,
                details = "Payment failed: $errorMessage"
            )
        )
    }

    fun logRefundStart(orderId: String) {
        auditService.record(
            AuditEvent(
                type = "REFUND_START",
                orderId = orderId,
                details = "Refund initiated"
            )
        )
    }

    fun logRefundSuccess(orderId: String) {
        auditService.record(
            AuditEvent(
                type = "REFUND_SUCCESS",
                orderId = orderId,
                details = "Refund completed"
            )
        )
    }
}

interface AuditService {
    fun record(event: AuditEvent)
}

@Component
class DefaultAuditService : AuditService {
    override fun record(event: AuditEvent) {
        println("[AUDIT] ${event.type}: ${event.orderId} - ${event.details}")
    }
}

data class AuditEvent(
    val type: String,
    val orderId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
