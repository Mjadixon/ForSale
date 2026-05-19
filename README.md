# For Sale

A console Java game based on the **For Sale** card game. Two phases, four players, five rounds each. Play in an IDE or terminal with a **move log on the left** and the **game screen on the right**.

## Run

```bash
javac -d out src/forsale/*.java
java -cp out forsale.Main
```

In an IDE: run `forsale.Main` as the main class.

## Goal

In **Phase 2**, each player gives one property card; the **highest card** wins the **biggest check**, which is added to that player's total. After five selling rounds, whoever has the **most money** (**checks won + coins still in hand**) wins the game. All amounts use full dollars (e.g. **$15,000**).

## Setup

- Exactly **4 players** (each seat is human or AI).
- Each player starts with **$18,000**.
- Property cards rank **1–20** (higher number = more desirable).

## Phase 1 — Bidding (5 rounds)

Each round:

1. **4 property cards** are dealt face up (sorted low → high).
2. Players take turns **bidding** or **passing** until only one bidder remains.
3. **Pass:** take the **lowest** card still on the table; recover **half** your bid (rounded down); the rest leaves the game.
4. **Last bidder:** take the **highest** card; pay your **full** bid to the bank.

After **5 rounds**, all 20 property cards are distributed. Phase 2 begins.

## Phase 2 — Selling (5 rounds)

Each round:

1. **4 checks** are dealt face up (highest value first).
2. Each player **gives** (plays) **one property card** from Phase 1, face down.
3. Everyone **reveals** together.
4. The **highest property** wins the **top check** — that check is **added to their total**.
5. Second-highest property wins the second-biggest check, and so on.

Properties are sold and removed; you keep the checks. Check deck: two copies of each value 1–20 (total **$420,000**).

**Winner:** player with the highest combined total of **checks earned + leftover coins**.

## Controls & commands

| When | Input | Action |
|------|--------|--------|
| Setup | `y` / `n` | Human or AI for this seat |
| Setup | name | Display name (human players) |
| Bidding | **Enter** | Bid the minimum — automatically **$1,000 over** the current high bid |
| Bidding | `0` or `pass` | Pass |
| Bidding | number | **Total** bid in thousands (`3` = **$3,000**) |
| Bidding | `help` | Show bidding help |
| Selling | number | Pick property by list number (1, 2, 3…) |
| Any prompt | **Enter** | Continue |

### Bidding example

- High bid is **$2,000** → next legal bid is **$3,000**.
- Press **Enter** to bid **$3,000** (you do not need to type `3`).
- Type **`5`** to bid **$5,000** total instead.
- Type **`0`** or **`pass`** to pass.

## Screen layout

```
-- Moves --                          | -- Game --
Alice bids $3,000                    | Next bid: $4,000
Bob passed → #2                      | Enter = bid $4,000
...
```

The left column records every deal, bid, pass, and reveal.

## Project structure

| Class | Role |
|-------|------|
| `Main` | Entry point |
| `Game` | Setup and flow |
| `GameRules` | 4 players, 5 rounds, 4 cards |
| `GameHelp` | Rules text and help screens |
| `BiddingPhase` / `BiddingRound` | Phase 1 |
| `SellingPhase` / `SellingRound` | Phase 2 |
| `ConsoleUI` / `MoveLog` | Split display and input |
| `HumanController` / `AiController` | Player decisions |
| `Currency` | Dollar formatting |

## AI

Computer players use table strength, property rank, cash reserves, and how many bidders remain to decide whether to bid, pass, or raise. In selling, they play stronger properties when high checks are on the table.
