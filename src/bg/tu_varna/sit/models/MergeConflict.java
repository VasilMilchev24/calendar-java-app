package bg.tu_varna.sit.models;

/**
 * Представя конфликт между текущо и входящо събитие при сливане.
 */
public class MergeConflict {

    /**
     * Съществуващо събитие от основния календар.
     */
    private final Event existingEvent;

    /**
     * Входящо събитие от вторичния календар.
     */
    private final Event incomingEvent;

    /**
     * Създава обект, описващ конфликт при сливане.
     *
     * @param existingEvent конфликтно събитие от текущия календар
     * @param incomingEvent конфликтно събитие от импортнатия календар
     */
    public MergeConflict(Event existingEvent, Event incomingEvent) {
        this.existingEvent = existingEvent;
        this.incomingEvent = incomingEvent;
    }

    /**
     * Връща съществуващото събитие.
     *
     * @return съществуващо събитие
     */
    public Event getExistingEvent() {
        return existingEvent;
    }

    /**
     * Връща входящото събитие.
     *
     * @return входящо събитие
     */
    public Event getIncomingEvent() {
        return incomingEvent;
    }
}

