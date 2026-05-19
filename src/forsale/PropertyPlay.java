package forsale;

/**
 * One face-down property choice during a selling round, before reveal.
 */
public class PropertyPlay {
    private final GameParticipant participant;
    private final PropertyCard property;

    public PropertyPlay(GameParticipant participant, PropertyCard property) {
        this.participant = participant;
        this.property = property;
    }

    public GameParticipant getParticipant() {
        return participant;
    }

    public Player getPlayer() {
        return participant.getPlayer();
    }

    public PropertyCard getProperty() {
        return property;
    }
}
