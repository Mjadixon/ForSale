package forsale;

/**
 * Chooses which property to sell face-down in Phase 2.
 */
public interface SellController {
    PropertyCard chooseProperty(SellContext context);
}
