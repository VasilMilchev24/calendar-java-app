package bg.tu_varna.sit.exceptions;

/**
 * Сигнализира за проблеми при файлов вход/изход и XML парсване при съхранение на календара.
 */
public class CalendarFileException extends Exception {

    /**
     * Създава изключение със съобщение.
     *
     * @param message съобщение за грешка
     */
    public CalendarFileException(String message) {
        super(message);
    }

    /**
     * Създава изключение със съобщение и първопричина.
     *
     * @param message съобщение за грешка
     * @param cause първопричина
     */
    public CalendarFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

