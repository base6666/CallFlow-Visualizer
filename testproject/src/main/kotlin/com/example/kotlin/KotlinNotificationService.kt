package com.example.kotlin

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/**
 * Kotlin notification service with async support
 */
@Service
class KotlinNotificationService(
    private val emailSender: EmailSender,
    private val smsSender: SmsSender,
    private val pushNotifier: PushNotifier
) {
    @Async
    fun sendOrderConfirmation(order: Order) {
        val message = buildConfirmationMessage(order)
        emailSender.send(order.id, "Order Confirmed", message)
        pushNotifier.notify(order.id, "Your order ${order.id} has been confirmed!")
    }

    @Async
    fun sendCancellationNotice(order: Order) {
        val message = buildCancellationMessage(order)
        emailSender.send(order.id, "Order Cancelled", message)
        smsSender.send(order.id, "Your order ${order.id} has been cancelled.")
    }

    @Async
    fun sendShippingUpdate(order: Order, trackingNumber: String) {
        val message = "Your order ${order.id} has been shipped. Tracking: $trackingNumber"
        emailSender.send(order.id, "Order Shipped", message)
        pushNotifier.notify(order.id, message)
    }

    private fun buildConfirmationMessage(order: Order): String {
        return """
            Thank you for your order!
            Order ID: ${order.id}
            Product: ${order.productId}
            Quantity: ${order.quantity}
            Status: ${order.status}
        """.trimIndent()
    }

    private fun buildCancellationMessage(order: Order): String {
        return """
            Your order has been cancelled.
            Order ID: ${order.id}
            If you have any questions, please contact support.
        """.trimIndent()
    }
}

interface EmailSender {
    fun send(recipient: String, subject: String, body: String)
}

interface SmsSender {
    fun send(phoneNumber: String, message: String)
}

interface PushNotifier {
    fun notify(userId: String, message: String)
}

@Service
class DefaultEmailSender : EmailSender {
    override fun send(recipient: String, subject: String, body: String) {
        println("Sending email to $recipient: $subject")
    }
}

@Service
class DefaultSmsSender : SmsSender {
    override fun send(phoneNumber: String, message: String) {
        println("Sending SMS to $phoneNumber: $message")
    }
}

@Service
class DefaultPushNotifier : PushNotifier {
    override fun notify(userId: String, message: String) {
        println("Push notification to $userId: $message")
    }
}
