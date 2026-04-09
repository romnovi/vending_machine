package interview.entrust.vending

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class VendingMachineTest {

    private val machine = VendingMachine(
        initialCoins = mapOf(Coin.F2 to 1, Coin.F1 to 1, Coin.P50 to 2, Coin.P20 to 2, Coin.P10 to 1, Coin.P5 to 1, Coin.P2 to 1, Coin.P1 to 1),
        initialProducts = mapOf(Product.CokeCan to 5, Product.ChocolateBar to 3),
        prices = mapOf(Product.CokeCan to 100, Product.ChocolateBar to 75)
    )

    @Test
    fun `insertCoin returns updated credit`() {
        assertEquals(InsertResult.CreditUpdated(50), machine.insertCoin(Coin.P50))
    }

    @Test
    fun `multiple insertions accumulate credit`() {
        machine.insertCoin(Coin.P50)
        assertEquals(InsertResult.CreditUpdated(100), machine.insertCoin(Coin.P50))
    }

    @Test
    fun `exact payment dispenses product with no change`() {
        machine.insertCoin(Coin.P50)
        machine.insertCoin(Coin.P50)
        val result = assertIs<SelectResult.Success>(machine.selectItem(Product.CokeCan))
        assertEquals(Product.CokeCan, result.product)
        assertEquals(emptyList(), result.change)
    }

    @Test
    fun `overpayment dispenses product with correct change`() {
        machine.insertCoin(Coin.F1)
        val result = assertIs<SelectResult.Success>(machine.selectItem(Product.ChocolateBar))
        assertEquals(Product.ChocolateBar, result.product)
        assertEquals(25, result.change.sumOf { it.pence })
    }

    @Test
    fun `insufficient funds returns shortfall and preserves credit`() {
        machine.insertCoin(Coin.P50)
        val result = assertIs<SelectResult.InsufficientFunds>(machine.selectItem(Product.CokeCan))
        assertEquals(50, result.shortfallPence)
        assertEquals(InsertResult.CreditUpdated(100), machine.insertCoin(Coin.P50))
    }

    @Test
    fun `product not found returns ProductNotFound and preserves credit`() {
        val emptyMachine = VendingMachine()
        emptyMachine.insertCoin(Coin.F1)
        assertIs<SelectResult.ProductNotFound>(emptyMachine.selectItem(Product.CokeCan))
        assertEquals(InsertResult.CreditUpdated(300), emptyMachine.insertCoin(Coin.F2))
    }

    @Test
    fun `out of stock returns OutOfStock and preserves credit`() {
        val limitedMachine = VendingMachine(
            initialCoins = mapOf(Coin.P50 to 4),
            initialProducts = mapOf(Product.CokeCan to 1),
            prices = mapOf(Product.CokeCan to 100)
        )

        limitedMachine.insertCoin(Coin.P50)
        limitedMachine.insertCoin(Coin.P50)
        assertIs<SelectResult.Success>(limitedMachine.selectItem(Product.CokeCan))

        limitedMachine.insertCoin(Coin.P50)
        limitedMachine.insertCoin(Coin.P50)
        assertIs<SelectResult.OutOfStock>(limitedMachine.selectItem(Product.CokeCan))
    }

    @Test
    fun `cannot make change refunds inserted coins and leaves stock unchanged`() {
        val noChangeMachine = VendingMachine(
            initialProducts = mapOf(Product.ChocolateBar to 5),
            prices = mapOf(Product.ChocolateBar to 75)
        )

        noChangeMachine.insertCoin(Coin.F1)
        val result = assertIs<SelectResult.CannotMakeChange>(noChangeMachine.selectItem(Product.ChocolateBar))
        assertEquals(100, result.refund.sumOf { it.pence })

        noChangeMachine.addCoins(mapOf(Coin.P20 to 1, Coin.P5 to 1))
        noChangeMachine.insertCoin(Coin.F1)
        assertIs<SelectResult.Success>(noChangeMachine.selectItem(Product.ChocolateBar))
    }

    @Test
    fun `cancel returns all inserted coins and resets credit`() {
        machine.insertCoin(Coin.P50)
        machine.insertCoin(Coin.P20)
        val refund = machine.cancel()
        assertEquals(70, refund.sumOf { it.pence })
        assertEquals(InsertResult.CreditUpdated(10), machine.insertCoin(Coin.P10))
    }

    @Test
    fun `addProduct restocks after out of stock`() {
        val limitedMachine = VendingMachine(
            initialCoins = mapOf(Coin.P50 to 4),
            initialProducts = mapOf(Product.CokeCan to 1),
            prices = mapOf(Product.CokeCan to 100)
        )

        limitedMachine.insertCoin(Coin.P50)
        limitedMachine.insertCoin(Coin.P50)
        assertIs<SelectResult.Success>(limitedMachine.selectItem(Product.CokeCan))

        limitedMachine.insertCoin(Coin.P50)
        limitedMachine.insertCoin(Coin.P50)
        assertIs<SelectResult.OutOfStock>(limitedMachine.selectItem(Product.CokeCan))

        limitedMachine.addProduct(Product.CokeCan, 1)
        assertIs<SelectResult.Success>(limitedMachine.selectItem(Product.CokeCan))
    }

    @Test
    fun `price not found returns PriceNotFound and preserves credit`() {
        val noPriceMachine = VendingMachine(
            initialProducts = mapOf(Product.CokeCan to 1)
        )
        noPriceMachine.insertCoin(Coin.F1)
        assertIs<SelectResult.PriceNotFound>(noPriceMachine.selectItem(Product.CokeCan))
        assertEquals(InsertResult.CreditUpdated(300), noPriceMachine.insertCoin(Coin.F2))
    }

    @Test
    fun `setPrice allows purchase at updated price`() {
        machine.setPrice(Product.CokeCan, 80)
        machine.insertCoin(Coin.P50)
        machine.insertCoin(Coin.P20)
        machine.insertCoin(Coin.P10)
        val result = assertIs<SelectResult.Success>(machine.selectItem(Product.CokeCan))
        assertEquals(0, result.change.sumOf { it.pence })
    }

    @Test
    fun `setPrice replaces existing price`() {
        machine.insertCoin(Coin.P50)
        machine.insertCoin(Coin.P50)
        assertIs<SelectResult.Success>(machine.selectItem(Product.CokeCan))

        machine.setPrice(Product.CokeCan, 150)
        machine.insertCoin(Coin.P50)
        machine.insertCoin(Coin.P50)
        assertIs<SelectResult.InsufficientFunds>(machine.selectItem(Product.CokeCan))
    }

    @Test
    fun `products returns current stock quantities`() {
        assertEquals(mapOf(Product.CokeCan to 5, Product.ChocolateBar to 3), machine.products())
    }

    @Test
    fun `prices returns configured prices`() {
        assertEquals(mapOf(Product.CokeCan to 100, Product.ChocolateBar to 75), machine.prices())
    }

    @Test
    fun `sequential purchases maintain consistent state`() {
        machine.insertCoin(Coin.P50)
        machine.insertCoin(Coin.P50)
        assertIs<SelectResult.Success>(machine.selectItem(Product.CokeCan))

        machine.insertCoin(Coin.P50)
        machine.insertCoin(Coin.P50)
        assertIs<SelectResult.Success>(machine.selectItem(Product.CokeCan))
    }
}
