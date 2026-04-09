package interview.entrust.vending

enum class Coin(val pence: Int) {
    P1(1),
    P2(2),
    P5(5),
    P10(10),
    P20(20),
    P50(50),
    F1(100),
    F2(200)
}

sealed class InsertResult {
    data class CreditUpdated(val totalPence: Int) : InsertResult()
}
