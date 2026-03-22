package bg.tu_varna.sit.repository;

import bg.tu_varna.sit.exceptions.CalendarFileException;
import bg.tu_varna.sit.models.CalendarContext;

/**
 * Абстракция за съхранение на календарни данни.
 */
public interface CalendarRepository {

    /**
     * Зарежда календарни данни от подадения път.
     *
     * @param filePath път до XML файл
     * @return зареден календарен контекст
     * @throws CalendarFileException при неуспешно прочитане или парсване
     */
    CalendarContext load(String filePath) throws CalendarFileException;

    /**
     * Записва календарни данни в подадения път.
     *
     * @param filePath път до XML файл
     * @param context контекст за запис
     * @throws CalendarFileException при неуспешен запис
     */
    void save(String filePath, CalendarContext context) throws CalendarFileException;
}

