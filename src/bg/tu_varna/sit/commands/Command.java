package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.util.Scanner;

/**
 * Договор за изпълнима конзолна команда.
 */
public interface Command {

    /**
     * Изпълнява команда с парсирани аргументи.
     *
     * @param args парсирани аргументи, включително името на командата на позиция 0
     * @param context контекст на календара в паметта
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при грешка при изпълнение
     */
    void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception;
}

