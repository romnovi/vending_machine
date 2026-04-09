package interview.entrust.vending


sealed class SelectResult {
    data class Success(val product: Product, val change: List<Coin>) : SelectResult()
    data class InsufficientFunds(val shortfallPence: Int) : SelectResult()
    data class CannotMakeChange(val refund: List<Coin>) : SelectResult()
    object ProductNotFound : SelectResult()
    object OutOfStock : SelectResult()
    object PriceNotFound : SelectResult()
}

class VendingMachine(
    initialCoins: Map<Coin, Int> = emptyMap(),
    initialProducts: Map<Product, Int> = emptyMap(),
    prices: Map<Product, Int> = emptyMap(),
) {
    private val coinInventory = CoinInventory().apply { addCoins(initialCoins) }
    private val productInventory = ProductInventory(initialProducts)
    private val insertedCoins: MutableList<Coin> = mutableListOf()
    private val prices: MutableMap<Product, Int> = prices.toMutableMap()

    fun insertCoin(coin: Coin): InsertResult {
        insertedCoins.add(coin)
        return InsertResult.CreditUpdated(insertedCoins.sumOf { it.pence })
    }

    fun selectItem(product: Product): SelectResult {
        val quantity = productInventory.getQuantity(product) ?: return SelectResult.ProductNotFound
        if (quantity == 0) return SelectResult.OutOfStock
        val price = prices[product] ?: return SelectResult.PriceNotFound

        val credit = insertedCoins.sumOf { it.pence }
        val changeAmount = credit - price

        return when {
            changeAmount < 0 -> SelectResult.InsufficientFunds(-changeAmount)
            changeAmount > 0 && !coinInventory.canMakeChange(changeAmount, insertedCoins) -> {
                SelectResult.CannotMakeChange(insertedCoins.toList().also { insertedCoins.clear() })
            }
            else -> SelectResult.Success(product, dispense(product, changeAmount))
        }
    }

    fun cancel(): List<Coin> {
        val refund = insertedCoins.toList()
        insertedCoins.clear()
        return refund
    }

    fun products(): Map<Product, Int> = productInventory.stock()
    fun prices(): Map<Product, Int> = prices.toMap()

    fun addProduct(product: Product, quantity: Int) {
        productInventory.addStock(product, quantity)
    }

    fun addCoins(coins: Map<Coin, Int>) = coinInventory.addCoins(coins)

    fun setPrice(product: Product, pence: Int) { prices[product] = pence }

    private fun dispense(product: Product, changeAmount: Int): List<Coin> {
        coinInventory.addCoins(insertedCoins)
        insertedCoins.clear()
        productInventory.dispense(product)
        return if (changeAmount > 0) coinInventory.makeChange(changeAmount) else emptyList()
    }
}
