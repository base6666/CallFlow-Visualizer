package com.example.kotlin

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType

@Entity
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val productId: String = "",
    val quantity: Int = 0,
    var status: OrderStatus = OrderStatus.PENDING
) {
    fun markAsConfirmed() {
        status = OrderStatus.CONFIRMED
    }

    fun markAsShipped() {
        status = OrderStatus.SHIPPED
    }

    fun cancel() {
        status = OrderStatus.CANCELLED
    }
}

enum class OrderStatus {
    PENDING, CONFIRMED, SHIPPED, CANCELLED
}
