package forsale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shuffled deck of property cards with values 1 through 20.
 */
public class PropertyDeck {
    private final List<PropertyCard> cards = new ArrayList<>();
    private final Random random;

    public PropertyDeck() {
        this(new Random());
    }

    public PropertyDeck(Random random) {
        this.random = random;
        for (int value = 1; value <= 20; value++) {
            cards.add(new PropertyCard(value));
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int remaining() {
        return cards.size();
    }

    /** Draw one card from the top of the deck. */
    public PropertyCard draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty.");
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * Deal {@code count} cards for one bidding round.
     */
    public List<PropertyCard> dealRound(int count) {
        if (cards.size() < count) {
            throw new IllegalStateException("Not enough cards left to deal this round.");
        }
        List<PropertyCard> dealt = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dealt.add(draw());
        }
        Collections.sort(dealt);
        return dealt;
    }
}
