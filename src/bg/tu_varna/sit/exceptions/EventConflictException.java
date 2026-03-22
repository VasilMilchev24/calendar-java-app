package bg.tu_varna.sit.exceptions;

/**
 * Сигнализира, че събитие се припокрива с друго вече записано събитие.
 */
public class EventConflictException extends Exception {

    /**
     * Създава изключение със съобщение.
     *
     * @param message съобщение за грешка
     */
    public EventConflictException(String message) {
        super(message);
    }
}

