package interview.entrust.vending.inventory

import interview.entrust.vending.domain.Product

class ProductInventory(initialStock: Map<Product, Int> = emptyMap()) {
    private val stock: MutableMap<Product, Int> = initialStock.toMutableMap()

    fun addStock(product: Product, quantity: Int) {
        stock[product] = (stock[product] ?: 0) + quantity
    }

    fun getQuantity(product: Product): Int? = stock[product]
    fun stock(): Map<Product, Int> = stock.toMap()

    fun dispense(product: Product): Product {
        val quantity = stock[product] ?: throw IllegalArgumentException("Product not loaded: ${product.code}")
        check(quantity > 0) { "Product out of stock: ${product.code}" }
        stock[product] = quantity - 1
        return product
    }
}
