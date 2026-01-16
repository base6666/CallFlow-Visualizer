package com.example.kotlin

import org.springframework.stereotype.Component

/**
 * Kotlin payment processing service
 */
@Component
class PaymentProcessor(
    private val paymentGateway: PaymentGateway,
    private val transactionLogger: TransactionLogger
) {
    fun processPayment(orderId: String, amount: Double): PaymentResult {
        transactionLogger.logStart(orderId, amount)

        val result = paymentGateway.charge(orderId, amount)

        if (result.success) {
            transactionLogger.logSuccess(orderId, result.transactionId)
        } else {
            transactionLogger.logFailure(orderId, result.errorMessage)
        }

        return result
    }

    fun refund(orderId: String): RefundResult {
        transactionLogger.logRefundStart(orderId)

        val result = paymentGateway.refund(orderId)

        if (result.success) {
            transactionLogger.logRefundSuccess(orderId)
        }

        return result
    }
}

interface PaymentGateway {
    fun charge(orderId: String, amount: Double): PaymentResult
    fun refund(orderId: String): RefundResult
}

@Component
class StripePaymentGateway : PaymentGateway {
    override fun charge(orderId: String, amount: Double): PaymentResult {
        // Simulate Stripe API call
        return PaymentResult(
            success = true,
            transactionId = "TXN-${System.currentTimeMillis()}",
            errorMessage = null
        )
    }

    override fun refund(orderId: String): RefundResult {
        return RefundResult(success = true, refundId = "REF-${System.currentTimeMillis()}")
    }
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val errorMessage: String?
)

data class RefundResult(
    val success: Boolean,
    val refundId: String?
)
