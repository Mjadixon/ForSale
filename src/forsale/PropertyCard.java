package forsale;

/**
 * A property card used in Phase 1 bidding. Values 1–20; higher is more desirable.
 */
public class PropertyCard implements Comparable<PropertyCard> {
    private final int value;

    public PropertyCard(int value) {
        if (value < 1 || value > 20) {
            throw new IllegalArgumentException("Property value must be between 1 and 20.");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int compareTo(PropertyCard other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "Property #" + value + " (rank " + value + "/20)";
    }
}
