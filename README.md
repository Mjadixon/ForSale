# For Sale

Console Java game — 4 players, 2 phases, **5 rounds / 5 check batches**. **Highest balance wins.**

## Run

```bash
javac -d out src/forsale/*.java
java -cp out forsale.Main
```

IDE: run `forsale.Main`.

## Goal

**Highest balance** = checks from Phase 2 + coins left from Phase 1.

## Phase 1 — Bidding (5 rounds)

- 4 property cards per round (higher # = better).
- Bid or pass until one bidder remains.
- **Pass:** lowest card + half your bid back.
- **Last bidder:** highest card + pay full bid.

## Phase 2 — Selling (5 batches)

Each of the **5 batches**:

1. **4 checks** on the table (rank 1 = biggest).
2. Each player **selects one property** they won in Phase 1 (your list is highest → lowest).
3. All reveal together.
4. **Rank by property #** — highest wins the top check, 2nd wins the next, etc.
5. Each check value is **added to that player's balance** (shown as `+ $X,000`).

## Commands

| When | Input | Action |
|------|--------|--------|
| Setup | `y` / `n` | Human or AI |
| Bidding | **Enter** | Minimum bid |
| Bidding | `0` / `pass` | Pass |
| Bidding | number | Total bid (`3` = $3,000) |
| Selling | number | Pick property (`1` = your highest) |
| **Any time** | **help** | Open rules menu, then continue playing |
| Continue | **Enter** | Next step |

## Win

Player with the **highest balance** (checks + coins) wins.
