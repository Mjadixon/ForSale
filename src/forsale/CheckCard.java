package forsale;

/**
 * A bank check paid out in Phase 2 (stored in thousands: 15 = $15,000).
 */
public class CheckCard implements Comparable<CheckCard> {
    private final int valueGrands;

    public CheckCard(int valueGrands) {
        if (valueGrands < 1) {
            throw new IllegalArgumentException("Check value must be at least $1,000.");
        }
        this.valueGrands = valueGrands;
    }

    public int getValueGrands() {
        return valueGrands;
    }

    @Override
    public int compareTo(CheckCard other) {
        return Integer.compare(this.valueGrands, other.valueGrands);
    }

    @Override
    public String toString() {
        return "Check " + Currency.format(valueGrands);
    }
}
