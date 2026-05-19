# For Sale

Console Java game (4 players, human and/or AI). **Highest balance at the end wins.**

## Run

```bash
javac -d out src/forsale/*.java
java -cp out forsale.Main
```

IDE: run `forsale.Main`.

## Goal

**Highest balance** = checks you won in Phase 2 + coins still in hand.

## Phase 1 — Bidding (5 rounds)

- 4 property cards per round (higher # = better).
- Bid or pass until one bidder remains.
- **Pass:** take lowest card, recover half your bid.
- **Last bidder:** take highest card, pay full bid.

## Phase 2 — Selling (until all cards are gone)

1. **Checks** are put on the table (best check first).
2. For **each check**, from **highest to lowest**:
   - Every player still holding properties **selects one card** they won in Phase 1.
   - Your list is shown **highest to lowest** (1 = your best card).
   - Everyone reveals together.
   - The **highest property number** wins **that check** (added to balance).
   - Played cards are removed; move to the next check.
3. When checks on the table are done, deal another batch if anyone still has cards.
4. Continue until **every property card has been played**.

## Controls

| When | Input | Action |
|------|--------|--------|
| Setup | `y` / `n` | Human or AI |
| Bidding | **Enter** | Minimum bid ($1,000 over last) |
| Bidding | `0` / `pass` | Pass |
| Bidding | number | Total bid in thousands (`3` = $3,000) |
| Selling | number | Pick property (**1** = your **highest** card) |
| Anywhere | **Enter** | Continue |

## Screen

Left column = move log. Right column = game. Amounts always show full dollars (**$5,000**).

## Win

Player with the **highest balance** (checks + coins) wins.
