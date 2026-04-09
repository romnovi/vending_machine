# Vending Machine

A pure Kotlin library implementing a vending machine. Accepts coin insertions and product selections, dispenses products, and returns change using UK coin denominations. All amounts are in pence.

## Interaction Flow

The expected flow is: **insert coins first, then select a product.**

1. Call `insertCoin()` one or more times to build up credit.
2. Call `selectItem()` to attempt a purchase.
3. On success, the product and any change are returned. On failure, credit is preserved so the user can insert more coins or cancel.

```
insertCoin(P50) → CreditUpdated(50)
insertCoin(P50) → CreditUpdated(100)
selectItem(CokeCan) → Success(product=CokeCan, change=[])
```

Calling `selectItem()` before inserting coins will return `InsufficientFunds` — credit is not reset, so the user can continue inserting coins afterwards.

## Edge Cases

**Cannot make change** — if the machine cannot give exact change after a purchase, the transaction is refused and all inserted coins are returned in full. The product stock is not decremented.

```
insertCoin(F1)
selectItem(ChocolateBar)  // price 75p, machine has no 25p change
→ CannotMakeChange(refund=[F1])
```

After adding change to the machine, the same purchase can succeed:

```
addCoins([P20, P5])
insertCoin(F1)
selectItem(ChocolateBar) → Success(product=ChocolateBar, change=[P20, P5])
```

**Cancel** — `cancel()` can be called at any time to return all inserted coins and reset credit to zero.

## Usage

```kotlin
val machine = VendingMachine(
    initialCoins = mapOf(Coin.F1 to 5, Coin.P50 to 10, Coin.P20 to 10, Coin.P10 to 10, Coin.P5 to 10),
    initialProducts = mapOf(Product.CokeCan to 5, Product.ChocolateBar to 3),
    prices = mapOf(Product.CokeCan to 100, Product.ChocolateBar to 75)
)

machine.insertCoin(Coin.F1)  // InsertResult.CreditUpdated(100)

when (val result = machine.selectItem(Product.CokeCan)) {
    is SelectResult.Success -> println("Dispensed ${result.product.name}, change: ${result.change}")
    is SelectResult.InsufficientFunds -> println("Need ${result.shortfallPence}p more")
    is SelectResult.CannotMakeChange -> println("Refunded: ${result.refund}")
    is SelectResult.OutOfStock -> println("Out of stock")
    is SelectResult.ProductNotFound -> println("Product not found")
    is SelectResult.PriceNotFound -> println("No price configured")
}

// Cancel and get coins back
val refund = machine.cancel()
```

## Coins

| Constant | Value |
|----------|-------|
| `P1`     | 1p    |
| `P2`     | 2p    |
| `P5`     | 5p    |
| `P10`    | 10p   |
| `P20`    | 20p   |
| `P50`    | 50p   |
| `F1`     | £1    |
| `F2`     | £2    |

## Prices

Prices can be supplied at construction time and updated at any point via `setPrice`:

```kotlin
machine.setPrice(Product.CokeCan, 120)
```

## Single Global State

`VendingMachine` holds one shared state: coin float, product stock, prices, and the currently inserted credit all live in a single instance.

## Thread Safety

`VendingMachine` is not thread-safe. All calls — including `insertCoin`, `selectItem`, and `cancel` — must be made from a single thread, or the caller must provide external synchronisation.

## Building & Testing

```bash
./gradlew build
./gradlew test
```

Requires JDK 21.
