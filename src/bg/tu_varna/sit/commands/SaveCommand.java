package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.util.Scanner;

/**
 * Записва текущия контекст в отворения файл.
 */
public class SaveCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public SaveCommand() {
    }

    /**
     * Изпълнява командата save.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис или неуспешен запис
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 1) {
            throw new InvalidCommandException("Употреба: save");
        }
        service.save(context);
        System.out.println("Записано във файл: " + service.getCurrentFilePath());
    }
}
