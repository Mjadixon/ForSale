# For Sale

Console Java game — 4 players, 2 phases. **Highest balance wins.**

## Run

```bash
javac -d out src/forsale/*.java
java -cp out forsale.Main
```

IDE: run `forsale.Main`.

## Phase 1 — Bidding (5 rounds)

- 4 property cards per round. Bid or pass.
- **Pass:** lowest card + half bid back.
- **Last bidder:** highest card + full bid paid.
- End of each round: summary shows **who won which card**.
- The **auction winner** starts bidding the **next** round.

## Phase 2 — Selling (1 batch per card won)

The number of **batches** equals the **properties you won** in Phase 1 (usually 5).

For **each batch** (step through one at a time):

1. Four **checks** are dealt (best first).
2. Each player still holding cards **selects one property** to play.
3. All **reveal** — ranked by property # (high to low).
4. **Rank 1** wins the top check, rank 2 the next, etc.
5. Check amounts are **added to your balance**.
6. **Press Enter** → go to the **next batch**.

Phase 2 ends when **every batch has been played** and **all property cards are gone**.

## Commands

| Input | Action |
|--------|--------|
| **Enter** | Continue / minimum bid |
| `0` / `pass` | Pass (bidding) |
| number | Bid total or pick property (`1` = your highest card) |
| **help** | Rules menu, then return to the game |

## Scoring

Each check you win is **added to your balance** (`balance before -> balance after`).

**Final scores** show:
- **Checks** — total from Phase 2  
- **Coins** — left from Phase 1  
- **TOTAL** — checks + coins (highest wins)

## Restart

After final scores, choose **y** to play again or **n** to quit.

## Win

Highest **total balance** on the final scoreboard.
