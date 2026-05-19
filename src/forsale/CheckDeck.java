package forsale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Check deck: two copies of each value 1–10 (matching property ranks).
 * Total value = (1 + 2 + … + 10) × 2 = 110 Grands.
 */
public class CheckDeck {
    public static final int COPIES_PER_VALUE = 2;
    public static final int MIN_VALUE = 1;
    public static final int MAX_VALUE = 10;

    private final List<CheckCard> cards = new ArrayList<>();
    private final Random random;

    public CheckDeck() {
        this(new Random());
    }

    public CheckDeck(Random random) {
        this.random = random;
        buildDeck();
        shuffle();
    }

    private void buildDeck() {
        for (int value = MIN_VALUE; value <= MAX_VALUE; value++) {
            for (int copy = 0; copy < COPIES_PER_VALUE; copy++) {
                cards.add(new CheckCard(value));
            }
        }
    }

    /** Sum of one copy of each value, times two. */
    public static int totalDeckValueGrands() {
        int sum = 0;
        for (int value = MIN_VALUE; value <= MAX_VALUE; value++) {
            sum += value;
        }
        return sum * COPIES_PER_VALUE;
    }

    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    public int remaining() {
        return cards.size();
    }

    public CheckCard draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Check deck is empty.");
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Deal checks for one selling round, highest value first.
     */
    public List<CheckCard> dealRound(int count) {
        if (cards.size() < count) {
            throw new IllegalStateException("Not enough checks left to deal this round.");
        }
        List<CheckCard> dealt = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dealt.add(draw());
        }
        dealt.sort(Collections.reverseOrder());
        return dealt;
    }
}
