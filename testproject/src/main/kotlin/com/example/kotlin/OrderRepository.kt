package com.example.kotlin

import org.springframework.stereotype.Repository

/**
 * Kotlin repository for order persistence
 */
@Repository
interface OrderRepository {
    fun save(order: Order): Order
    fun findById(orderId: Long): Order?
    fun update(order: Order): Order
    fun delete(orderId: Long): Boolean
    fun findByStatus(status: OrderStatus): List<Order>
}

/**
 * Default implementation of OrderRepository
 */
@Repository
class OrderRepositoryImpl : OrderRepository {
    private val orders = mutableMapOf<Long, Order>()

    override fun save(order: Order): Order {
        orders[order.id] = order
        return order
    }

    override fun findById(orderId: Long): Order? {
        return orders[orderId]
    }

    override fun update(order: Order): Order {
        orders[order.id] = order
        return order
    }

    override fun delete(orderId: Long): Boolean {
        return orders.remove(orderId) != null
    }

    override fun findByStatus(status: OrderStatus): List<Order> {
        return orders.values.filter { it.status == status }
    }
}
