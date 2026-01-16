package com.example.kotlin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
// Order and OrderStatus are imported from Order.kt

/**
 * Kotlin test service for call flow analysis
 */
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val paymentProcessor: PaymentProcessor,
    private val notificationService: KotlinNotificationService,
    private val inventoryService: InventoryService
) {

    @Transactional
    fun createOrder(request: OrderRequest): Order {
        // Validate request
        validateRequest(request)

        // Check inventory
        inventoryService.checkAvailability(request.productId, request.quantity)

        // Create order
        val order = Order(
            id = generateOrderId(),
            productId = request.productId,
            quantity = request.quantity
        )

        // Save order
        val savedOrder = orderRepository.save(order)

        // Process payment
        paymentProcessor.processPayment(savedOrder.id.toString(), request.amount)

        // Update status
        savedOrder.status = OrderStatus.CONFIRMED
        orderRepository.update(savedOrder)

        // Send notification
        notificationService.sendOrderConfirmation(savedOrder)

        return savedOrder
    }

    fun getOrder(orderId: Long): Order? {
        return orderRepository.findById(orderId)
    }

    fun cancelOrder(orderId: Long): Boolean {
        val order = orderRepository.findById(orderId) ?: return false

        if (order.status == OrderStatus.SHIPPED) {
            throw IllegalStateException("Cannot cancel shipped order")
        }

        // Refund payment
        paymentProcessor.refund(orderId.toString())

        // Cancel order using Entity method
        order.cancel()
        orderRepository.update(order)

        // Notify customer
        notificationService.sendCancellationNotice(order)

        return true
    }

    /**
     * Test method to demonstrate Entity method calls in call flow
     */
    fun processOrderLifecycle(orderId: String) {
        val order = orderRepository.findById(orderId) ?: return

        // Call Entity methods - should show Entity badge in call graph
        order.markAsConfirmed()
        orderRepository.update(order)

        order.markAsShipped()
        orderRepository.update(order)
    }

    private fun validateRequest(request: OrderRequest) {
        require(request.quantity > 0) { "Quantity must be positive" }
        require(request.amount > 0) { "Amount must be positive" }
    }

    private fun generateOrderId(): Long = System.currentTimeMillis()
}

data class OrderRequest(
    val productId: String,
    val quantity: Int,
    val amount: Double
)
// Order and OrderStatus are defined in Order.kt with proper JPA annotations
