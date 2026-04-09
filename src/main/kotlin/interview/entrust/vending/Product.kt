package interview.entrust.vending

data class Product(val code: String, val name: String) {
    companion object {
        val CokeCan = Product("COKE", "Coca-Cola Can")
        val ChocolateBar = Product("CHOC", "Chocolate Bar")
    }
}
