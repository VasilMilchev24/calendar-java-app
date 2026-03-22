package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.util.Scanner;

/**
 * Затваря текущия календар и изчиства данните в паметта.
 */
public class CloseCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public CloseCommand() {
    }

    /**
     * Изпълнява командата close.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 1) {
            throw new InvalidCommandException("Употреба: close");
        }
        service.close(context);
        System.out.println("Календарът е затворен.");
    }
}
