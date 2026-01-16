package com.example.kotlin

import org.springframework.stereotype.Service

/**
 * Kotlin inventory management service
 */
@Service
class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val warehouseClient: WarehouseClient
) {
    fun checkAvailability(productId: String, quantity: Int): Boolean {
        val inventory = inventoryRepository.findByProductId(productId)
            ?: throw ProductNotFoundException("Product $productId not found")

        if (inventory.availableQuantity < quantity) {
            throw InsufficientInventoryException(
                "Requested $quantity but only ${inventory.availableQuantity} available"
            )
        }

        return true
    }

    fun reserveInventory(productId: String, quantity: Int): ReservationResult {
        val inventory = inventoryRepository.findByProductId(productId)
            ?: throw ProductNotFoundException("Product $productId not found")

        if (inventory.availableQuantity < quantity) {
            return ReservationResult(success = false, reservationId = null)
        }

        inventory.availableQuantity -= quantity
        inventory.reservedQuantity += quantity
        inventoryRepository.update(inventory)

        return ReservationResult(
            success = true,
            reservationId = "RES-${System.currentTimeMillis()}"
        )
    }

    fun releaseReservation(reservationId: String) {
        // Release logic
        warehouseClient.notifyRelease(reservationId)
    }

    fun syncWithWarehouse(productId: String) {
        val warehouseQuantity = warehouseClient.getQuantity(productId)
        val inventory = inventoryRepository.findByProductId(productId)

        if (inventory != null) {
            inventory.availableQuantity = warehouseQuantity
            inventoryRepository.update(inventory)
        }
    }
}

interface InventoryRepository {
    fun findByProductId(productId: String): Inventory?
    fun update(inventory: Inventory): Inventory
}

interface WarehouseClient {
    fun getQuantity(productId: String): Int
    fun notifyRelease(reservationId: String)
}

@Service
class InventoryRepositoryImpl : InventoryRepository {
    private val inventories = mutableMapOf<String, Inventory>()

    override fun findByProductId(productId: String): Inventory? {
        return inventories[productId]
    }

    override fun update(inventory: Inventory): Inventory {
        inventories[inventory.productId] = inventory
        return inventory
    }
}

@Service
class DefaultWarehouseClient : WarehouseClient {
    override fun getQuantity(productId: String): Int = 100
    override fun notifyRelease(reservationId: String) {
        println("Warehouse notified of release: $reservationId")
    }
}

data class Inventory(
    val productId: String,
    var availableQuantity: Int,
    var reservedQuantity: Int
)

data class ReservationResult(
    val success: Boolean,
    val reservationId: String?
)

class ProductNotFoundException(message: String) : RuntimeException(message)
class InsufficientInventoryException(message: String) : RuntimeException(message)
