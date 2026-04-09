package interview.entrust.vending.domain


sealed class InsertResult {
    data class CreditUpdated(val totalPence: Int) : InsertResult()
}

sealed class SelectResult {
    data class Success(val product: Product, val change: List<Coin>) : SelectResult()
    data class InsufficientFunds(val shortfallPence: Int) : SelectResult()
    data class CannotMakeChange(val refund: List<Coin>) : SelectResult()
    object ProductNotFound : SelectResult()
    object OutOfStock : SelectResult()
    object PriceNotFound : SelectResult()
}
