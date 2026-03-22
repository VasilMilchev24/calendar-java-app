package bg.tu_varna.sit.exceptions;

/**
 * Сигнализира за невалиден синтаксис на команда или неподдържани аргументи.
 */
public class InvalidCommandException extends Exception {

    /**
     * Създава изключение със съобщение.
     *
     * @param message съобщение за грешка
     */
    public InvalidCommandException(String message) {
        super(message);
    }
}

