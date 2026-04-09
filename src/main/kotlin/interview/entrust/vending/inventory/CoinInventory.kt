package interview.entrust.vending.inventory

import interview.entrust.vending.domain.Coin
import java.util.TreeMap

class CoinInventory(initialStock: Map<Coin, Int> = emptyMap()) {
    private val coins: TreeMap<Coin, Int> = TreeMap<Coin, Int>(compareByDescending { it.pence })
        .apply { putAll(initialStock) }

    fun addCoins(coinsToAdd: List<Coin>) {
        coinsToAdd.forEach { coins.merge(it, 1, Int::plus) }
    }

    fun addCoins(coinsToAdd: Map<Coin, Int>) {
        coinsToAdd.forEach { (coin, count) -> coins.merge(coin, count, Int::plus) }
    }

    fun canMakeChange(amount: Int, extraCoins: List<Coin> = emptyList()): Boolean {
        val working = workingCopy().apply {
            extraCoins.forEach { coin -> merge(coin, 1, Int::plus) }
        }
        return tryMakeChange(amount, working) != null
    }

    fun makeChange(amount: Int): List<Coin> {
        val working = workingCopy()
        val change = tryMakeChange(amount, working)
            ?: throw IllegalStateException("Cannot make change for ${amount}p")
        change.forEach { coin -> coins[coin] = coins.getValue(coin) - 1 }
        return change
    }

    private fun workingCopy() = TreeMap(coins)

    // Greedy largest-first is optimal for UK denominations: each denomination divides
    // or is close enough to the next that greedy never overshoots a reachable amount.
    private fun tryMakeChange(amount: Int, available: MutableMap<Coin, Int>): List<Coin>? {
        var remaining = amount
        val result = mutableListOf<Coin>()
        for ((coin, count) in available) {
            val use = minOf(count, remaining / coin.pence)
            repeat(use) { result.add(coin) }
            remaining -= use * coin.pence
            if (remaining == 0) return result
        }
        return if (remaining == 0) result else null
    }
}
