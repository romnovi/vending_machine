package interview.entrust.vending

import interview.entrust.vending.domain.Coin
import interview.entrust.vending.inventory.CoinInventory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoinInventoryTest {

    private lateinit var inventory: CoinInventory

    @BeforeEach
    fun setUp() {
        inventory = CoinInventory()
    }

    @ParameterizedTest(name = "makeChange({1}p) from {0} returns {2}")
    @MethodSource("makeChangeCases")
    fun `makeChange returns correct coins`(available: Map<Coin, Int>, amount: Int, expected: List<Coin>) {
        inventory.addCoins(available)
        assertEquals(expected, inventory.makeChange(amount))
    }

    @ParameterizedTest(name = "canMakeChange({1}p) from {0} = {2}")
    @MethodSource("canMakeChangeCases")
    fun `canMakeChange correctness`(available: Map<Coin, Int>, amount: Int, expected: Boolean) {
        inventory.addCoins(available)
        assertEquals(expected, inventory.canMakeChange(amount))
    }

    @Test
    fun `makeChange throws when change is impossible`() {
        assertThrows<IllegalStateException> { inventory.makeChange(10) }
    }

    @Test
    fun `makeChange reduces available coins`() {
        inventory.addCoins(mapOf(Coin.P10 to 2))
        inventory.makeChange(10)
        assertFalse(inventory.canMakeChange(20))
    }

    @Test
    fun `canMakeChangeWith considers extra coins not yet in inventory`() {
        assertFalse(inventory.canMakeChange(10))
        assertTrue(inventory.canMakeChange(10, listOf(Coin.P10)))
    }

    @Test
    fun `addCoins makes coins available for future change`() {
        inventory.addCoins(mapOf(Coin.P20 to 1, Coin.P10 to 1))
        assertTrue(inventory.canMakeChange(30))
    }

    @Test
    fun `addCoins with quantity map adds correct totals`() {
        inventory.addCoins(mapOf(Coin.P20 to 3, Coin.P5 to 2))
        assertTrue(inventory.canMakeChange(70))
        assertFalse(inventory.canMakeChange(75))
    }

    companion object {
        @JvmStatic
        fun makeChangeCases() = listOf(
            Arguments.of(mapOf(Coin.P10 to 1), 10, listOf(Coin.P10)),
            Arguments.of(mapOf(Coin.P50 to 1, Coin.P20 to 2, Coin.P10 to 1), 50, listOf(Coin.P50)),
            Arguments.of(mapOf(Coin.P20 to 2, Coin.P10 to 1), 50, listOf(Coin.P20, Coin.P20, Coin.P10)),
            Arguments.of(mapOf(Coin.P10 to 2, Coin.P5 to 1, Coin.P2 to 2, Coin.P1 to 1), 30, listOf(Coin.P10, Coin.P10, Coin.P5, Coin.P2, Coin.P2, Coin.P1)),
        )

        @JvmStatic
        fun canMakeChangeCases() = listOf(
            Arguments.of(mapOf(Coin.P10 to 1, Coin.P5 to 1), 15, true),
            Arguments.of(mapOf(Coin.F2 to 1), 10, false),
            Arguments.of(emptyMap<Coin, Int>(), 0, true),
            Arguments.of(mapOf(Coin.P20 to 2), 40, true),
        )
    }
}
