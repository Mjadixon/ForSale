package forsale;

/**
 * All money is stored in thousands internally (18 = $18,000).
 * Display always shows full dollar amounts with zeros.
 */
public final class Currency {
    private Currency() {}

    /** @param thousands internal amount (3 means $3,000) */
    public static String format(int thousands) {
        return "$" + String.format("%,d,000", thousands);
    }

    public static String formatDollars(int dollars) {
        return "$" + String.format("%,d", dollars);
    }

    public static int dollarsToThousands(int dollars) {
        return Math.floorDiv(dollars, 1_000);
    }

    public static int floorToThousands(double dollars) {
        return (int) Math.floor(dollars / 1_000.0);
    }
}
