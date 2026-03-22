package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.util.Scanner;

/**
 * Извежда помощен текст за командите.
 */
public class HelpCommand implements Command {

    /**
     * Подготвен помощен текст.
     */
    private final String helpText;

    /**
     * Създава командата за помощ.
     *
     * @param helpText текст за отпечатване
     */
    public HelpCommand(String helpText) {
        this.helpText = helpText;
    }

    /**
     * Изпълнява командата help.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) {
        System.out.println(helpText);
    }
}

